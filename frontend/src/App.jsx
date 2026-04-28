import React, { useState, useEffect, useRef } from 'react';
import { useAuth, useUser, UserButton } from '@clerk/react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, User, MessageCircle, ArrowRight, CircleDashed, Plus, Users, UserPlus } from 'lucide-react';
import { Button } from './components/ui/Button';
import { Input } from './components/ui/Input';
import { cn } from './lib/utils';
import AuthPage from './components/AuthPage';

const App = () => {
  const { isSignedIn, getToken } = useAuth();
  const { user } = useUser();

  const [showAuth, setShowAuth] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [showUsernameModal, setShowUsernameModal] = useState(false);
  const [friends, setFriends] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [groups, setGroups] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [messageInput, setMessageInput] = useState('');
  const messagesEndRef = useRef(null);

  // Auto-scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Initialize user and connect
  useEffect(() => {
    if (isSignedIn && user) {
      const init = async () => {
        const token = await getToken();
        try {
          const response = await fetch('/api/auth/sync', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              clerkUserId: user.id,
              email: user.primaryEmailAddress?.emailAddress,
              firstName: user.firstName,
              lastName: user.lastName,
              profileImageUrl: user.imageUrl
            })
          });
          const userData = await response.json();
          setCurrentUser(userData);

          if (!userData.username) {
            setShowUsernameModal(true);
          } else {
            await fetchAllData(token);
            connectWebSocket(token, userData);
          }
        } catch (err) {
          console.error('Init error:', err);
        }
      };
      init();
    }
  }, [isSignedIn, user]);

  const fetchAllData = async (token) => {
    try {
      const [friendsRes, requestsRes, groupsRes] = await Promise.all([
        fetch('/api/friends', { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch('/api/friends/requests/pending', { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch('/api/groups', { headers: { 'Authorization': `Bearer ${token}` } })
      ]);

      // Handle friends response
      if (!friendsRes.ok) {
        console.error('Failed to fetch friends:', friendsRes.status);
        setFriends([]);
      } else {
        const friendsData = await friendsRes.json();
        setFriends(Array.isArray(friendsData) ? friendsData : []);
      }

      // Handle pending requests response
      if (!requestsRes.ok) {
        console.error('Failed to fetch pending requests:', requestsRes.status);
        setPendingRequests([]);
      } else {
        const requestsData = await requestsRes.json();
        setPendingRequests(Array.isArray(requestsData) ? requestsData : []);
      }

      // Handle groups response
      if (!groupsRes.ok) {
        console.error('Failed to fetch groups:', groupsRes.status);
        setGroups([]);
      } else {
        const groupsData = await groupsRes.json();
        setGroups(Array.isArray(groupsData) ? groupsData : []);
      }
    } catch (err) {
      console.error('Fetch data error:', err);
      setFriends([]);
      setPendingRequests([]);
      setGroups([]);
    }
  };

  const connectWebSocket = (token, userData) => {
    const socket = new SockJS(`/ws?token=${token}`);
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        token: token
      },
      debug: () => {},
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      setConnected(true);
      setStompClient(client);

      const userId = userData?.clerkUserId || currentUser?.clerkUserId;

      // Subscribe to DM messages
      client.subscribe(`/user/${userId}/queue/messages`, (msg) => {
        const newMsg = JSON.parse(msg.body);
        if (selectedChat?.type === 'dm' &&
            (newMsg.senderClerkId === selectedChat.id || newMsg.receiverClerkId === selectedChat.id)) {
          setMessages(prev => [...prev, newMsg]);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
    };

    client.activate();
  };

  // Subscribe to group topics when groups are loaded
  useEffect(() => {
    if (stompClient && stompClient.connected && groups.length > 0) {
      groups.forEach(group => {
        stompClient.subscribe(`/topic/group/${group.id}`, (msg) => {
          const newMsg = JSON.parse(msg.body);
          if (selectedChat?.type === 'group' && selectedChat.id === newMsg.groupId) {
            setMessages(prev => [...prev, newMsg]);
          }
        });
      });
    }
  }, [groups, stompClient]);

  // Fetch messages when chat changes
  useEffect(() => {
    if (selectedChat && currentUser) {
      const fetchMessages = async () => {
        const token = await getToken();
        try {
          const url = selectedChat.type === 'dm'
            ? `/api/messages/dm/${currentUser.clerkUserId}/${selectedChat.id}`
            : `/api/messages/group/${selectedChat.id}`;
          const res = await fetch(url, { headers: { 'Authorization': `Bearer ${token}` } });
          setMessages(await res.json());
        } catch (err) {
          console.error('Fetch messages error:', err);
        }
      };
      fetchMessages();
    }
  }, [selectedChat]);

  const sendMessage = (e) => {
    e.preventDefault();
    if (!messageInput.trim() || !stompClient || !selectedChat || !currentUser) return;

    if (selectedChat.type === 'dm') {
      const msgObj = {
        senderClerkId: currentUser.clerkUserId,
        receiverClerkId: selectedChat.id,
        content: messageInput
      };
      stompClient.publish({ destination: '/app/chat', body: JSON.stringify(msgObj) });
    } else {
      const msgObj = {
        senderClerkId: currentUser.clerkUserId,
        groupId: selectedChat.id,
        content: messageInput
      };
      stompClient.publish({ destination: '/app/group-chat', body: JSON.stringify(msgObj) });
    }

    setMessageInput('');
  };

  // Username Modal Component
  const UsernameModal = () => {
    const [username, setUsername] = useState('');
    const [available, setAvailable] = useState(null);
    const [checking, setChecking] = useState(false);

    const checkAvailability = async (val) => {
      if (val.length < 3) { setAvailable(null); return; }
      setChecking(true);
      try {
        const token = await getToken();
        const res = await fetch(`/api/auth/check-username?username=${val}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        setAvailable(data.available);
      } catch (err) {
        console.error('Check username error:', err);
      }
      setChecking(false);
    };

    const handleSubmit = async (e) => {
      e.preventDefault();
      const token = await getToken();
      try {
        await fetch('/api/auth/profile', {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ username })
        });
        setCurrentUser(prev => ({ ...prev, username }));
        setShowUsernameModal(false);
        const newToken = await getToken();
        fetchAllData(newToken);
        connectWebSocket(newToken);
      } catch (err) {
        console.error('Set username error:', err);
      }
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-card p-6 rounded-2xl w-96 shadow-xl">
          <h2 className="text-xl font-bold mb-4">Choose a Username</h2>
          <p className="text-muted-foreground mb-4">This is how others will find you</p>
          <form onSubmit={handleSubmit}>
            <Input
              value={username}
              onChange={(e) => { setUsername(e.target.value); checkAvailability(e.target.value); }}
              placeholder="Username (min 3 chars)"
              className="mb-2"
            />
            {checking && <p className="text-sm text-muted-foreground">Checking...</p>}
            {!checking && available !== null && (
              <p className={`text-sm ${available ? 'text-green-500' : 'text-red-500'}`}>
                {available ? '✓ Available' : '✗ Already taken'}
              </p>
            )}
            <Button type="submit" disabled={!available || username.length < 3} className="mt-4 w-full">
              Save Username
            </Button>
          </form>
        </div>
      </div>
    );
  };

  // Login Screen
  if (!isSignedIn) {
    if (showAuth) {
      return <AuthPage />;
    }
    return (
      <main className="min-h-screen bg-background flex items-center justify-center relative overflow-hidden p-6">
        <div className="absolute top-0 left-0 w-full h-[500px] bg-gradient-to-b from-accent/[0.03] to-transparent pointer-events-none" />
        <div className="absolute -top-[300px] -right-[300px] w-[800px] h-[800px] rounded-full bg-accent/[0.04] blur-[100px] pointer-events-none" />

        <motion.div initial="hidden" animate="visible" variants={{
          hidden: { opacity: 0, y: 20 },
          visible: { opacity: 1, y: 0, transition: { duration: 0.7 } }
        }} className="w-full max-w-md relative z-10">
          <div className="flex justify-center mb-10">
            <div className="inline-flex items-center gap-3 rounded-full border border-accent/20 bg-accent/[0.03] px-5 py-2 shadow-sm backdrop-blur-sm">
              <motion.span animate={{ scale: [1, 1.3, 1], opacity: [1, 0.7, 1] }} transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }} className="h-2 w-2 rounded-full bg-accent" />
              <span className="font-mono text-xs uppercase tracking-[0.15em] text-accent font-medium">CipherChat</span>
            </div>
          </div>

          <div className="text-center mb-10">
            <h1 className="font-display text-5xl md:text-6xl text-foreground mb-4 leading-tight">
              Connect with <br/>
              <span className="gradient-text relative inline-block">
                Friends
                <div className="absolute bottom-1 left-0 h-3 w-full rounded-sm bg-gradient-to-r from-accent/15 to-accent-secondary/10 -z-10" />
              </span>
            </h1>
            <p className="text-muted-foreground text-lg">Secure messaging platform</p>
          </div>

          <div className="bg-card rounded-[2rem] p-8 shadow-xl shadow-accent/[0.05] border border-border/50 space-y-4">
            <Button
              type="button"
              size="lg"
              className="w-full group"
              onClick={() => setShowAuth(true)}
            >
              Sign In
              <ArrowRight className="ml-2 h-5 w-5 transition-transform group-hover:translate-x-1" />
            </Button>

            <div className="relative">
              <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-border" /></div>
              <div className="relative flex justify-center text-sm"><span className="px-2 bg-card text-muted-foreground">OR</span></div>
            </div>

            <Button
              type="button"
              size="lg"
              variant="secondary"
              className="w-full group"
              onClick={() => setShowAuth(true)}
            >
              Sign Up
              <ArrowRight className="ml-2 h-5 w-5 transition-transform group-hover:translate-x-1" />
            </Button>
          </div>
        </motion.div>
      </main>
    );
  }

  // Show username modal if needed
  if (showUsernameModal) {
    return <UsernameModal />;
  }

  // Main Discord-like Interface
  return (
    <div className="h-screen bg-background flex flex-col md:flex-row">
      {/* Sidebar */}
      <div className="w-full md:w-64 bg-card border-r border-border flex flex-col">
        {/* User Info */}
        <div className="p-4 border-b border-border">
          <div className="flex items-center gap-3">
            <img src={currentUser?.profileImageUrl} alt="" className="w-10 h-10 rounded-full" />
            <div className="flex-1 min-w-0">
              <p className="font-medium text-sm truncate">{currentUser?.username}</p>
              <p className="text-xs text-muted-foreground truncate">{currentUser?.email}</p>
            </div>
            <UserButton afterSignOutUrl="/" />
          </div>
        </div>

        {/* Navigation */}
        <div className="flex-1 overflow-y-auto p-4 space-y-6">
          {/* Pending Requests */}
          {pendingRequests.length > 0 && (
            <div>
              <h3 className="text-xs font-semibold text-muted-foreground mb-2 uppercase tracking-wider">
                Requests ({pendingRequests.length})
              </h3>
              {pendingRequests.map(req => (
                <div key={req.id} className="text-sm p-2 hover:bg-accent/10 rounded cursor-pointer">
                  <p>New friend request</p>
                </div>
              ))}
            </div>
          )}

          {/* Friends List */}
          <div>
            <h3 className="text-xs font-semibold text-muted-foreground mb-2 uppercase tracking-wider">
              Friends ({friends.length})
            </h3>
            {friends.map(friend => (
              <div
                key={friend.clerkUserId}
                className={`p-2 rounded hover:bg-accent/10 cursor-pointer ${selectedChat?.id === friend.clerkUserId ? 'bg-accent/10' : ''}`}
                onClick={() => setSelectedChat({ type: 'dm', id: friend.clerkUserId, name: friend.username })}
              >
                <div className="flex items-center gap-2">
                  <img src={friend.profileImageUrl} alt="" className="w-8 h-8 rounded-full" />
                  <span className="text-sm">{friend.username}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Groups List */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                Groups ({groups.length})
              </h3>
              <button className="text-accent hover:text-accent-secondary">
                <Plus className="w-4 h-4" />
              </button>
            </div>
            {groups.map(group => (
              <div
                key={group.id}
                className={`p-2 rounded hover:bg-accent/10 cursor-pointer ${selectedChat?.id === group.id ? 'bg-accent/10' : ''}`}
                onClick={() => setSelectedChat({ type: 'group', id: group.id, name: group.name })}
              >
                <div className="flex items-center gap-2">
                  <Users className="w-5 h-5 text-accent" />
                  <span className="text-sm">{group.name}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Add Friend Button */}
        <div className="p-4 border-t border-border">
          <AddFriendButton onFriendAdded={() => fetchAllData(getToken())} />
        </div>
      </div>

      {/* Chat Area */}
      <div className="flex-1 flex flex-col bg-[#F8FAFC] relative">
        {selectedChat ? (
          <>
            {/* Chat Header */}
            <div className="h-16 px-6 flex items-center border-b border-border/50 bg-card/50 backdrop-blur-md">
              <div className="flex items-center gap-3">
                {selectedChat.type === 'group' ? (
                  <Users className="w-6 h-6 text-accent" />
                ) : (
                  <User className="w-6 h-6 text-accent" />
                )}
                <div>
                  <h3 className="font-medium">{selectedChat.name}</h3>
                  <span className="text-xs text-muted-foreground">
                    {connected ? 'Connected' : 'Connecting...'}
                  </span>
                </div>
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 p-6 overflow-y-auto">
              <div className="space-y-4 max-w-4xl mx-auto">
                <AnimatePresence initial={false}>
                  {messages.map((msg, i) => {
                    const isMe = msg.senderClerkId === currentUser?.clerkUserId;
                    return (
                      <motion.div
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        key={i}
                        className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}
                      >
                        <div className={`px-4 py-2 rounded-2xl max-w-[80%] ${isMe ? 'bg-accent text-white rounded-br-sm' : 'bg-card border border-border rounded-bl-sm'}`}>
                          {msg.content}
                        </div>
                      </motion.div>
                    );
                  })}
                </AnimatePresence>
                <div ref={messagesEndRef} />
              </div>
            </div>

            {/* Message Input */}
            <div className="p-4 bg-background border-t border-border/50">
              <form onSubmit={sendMessage} className="max-w-4xl mx-auto flex gap-2">
                <Input
                  value={messageInput}
                  onChange={(e) => setMessageInput(e.target.value)}
                  placeholder="Type a message..."
                  className="flex-1"
                />
                <Button type="submit" disabled={!messageInput.trim() || !connected}>
                  <Send className="w-4 h-4" />
                </Button>
              </form>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <MessageCircle className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
              <h2 className="text-2xl font-bold mb-2">Select a chat</h2>
              <p className="text-muted-foreground">Choose a friend or group to start messaging</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

  // Add Friend Button Component
  const AddFriendButton = ({ onFriendAdded }) => {
    const [showInput, setShowInput] = useState(false);
    const [username, setUsername] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const { getToken } = useAuth();

    const handleAdd = async (e) => {
      e.preventDefault();
      setError('');
      setSuccess('');
      const token = await getToken();
      try {
        const res = await fetch(`/api/friends/request?receiverUsername=${username}`, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) {
          const msg = await res.text();
          setError(msg || 'Failed to send request');
          return;
        }
        setSuccess('Friend request sent!');
        setUsername('');
        setTimeout(() => {
          setShowInput(false);
          setSuccess('');
          onFriendAdded();
        }, 1500);
      } catch (err) {
        setError('Failed to send request');
        console.error('Add friend error:', err);
      }
    };

    return (
      <div>
        {showInput ? (
          <div className="space-y-2">
            <form onSubmit={handleAdd} className="flex gap-2">
              <Input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter username"
                className="h-8 text-sm"
              />
              <Button type="submit" size="sm">Add</Button>
            </form>
            {error && <p className="text-sm text-red-500">{error}</p>}
            {success && <p className="text-sm text-green-500">{success}</p>}
          </div>
        ) : (
          <Button
            onClick={() => setShowInput(true)}
            variant="secondary"
            size="sm"
            className="w-full"
          >
            <UserPlus className="w-4 h-4 mr-2" />
            Add Friend
          </Button>
        )}
      </div>
    );
  };

export default App;
