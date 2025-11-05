# Chatbot API Documentation

## Chatbot Endpoint

The chatbot uses Google's Gemini AI to provide intelligent responses about the automobile service management system.

### Ask Chatbot Question

**POST** `/api/chatbot/ask`

Ask the AI-powered chatbot a question about services, appointments, or general system usage.

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "question": "How do I book an appointment for my car?",
  "previousQuestions": [
    "What services do you offer?",
    "Do you service motorcycles?"
  ]
}
```

**Field Descriptions:**
- `question`: (Required) The current question to ask the chatbot
- `previousQuestions`: (Optional) Array of previous questions to provide conversation context

**Success Response (200 OK):**
```json
{
  "answer": "To book an appointment for your car, follow these steps:\n\n1. Log in to your account (or provide your contact details for anonymous booking)\n2. Click on 'Book Appointment' or navigate to the appointments section\n3. Select your preferred date and time slot\n4. Enter your vehicle details (type, registration number)\n5. Choose the service type (repair, maintenance, inspection, etc.)\n6. Add any special instructions about the service needed\n7. Submit your appointment\n\nYour appointment will be set to PENDING status and will be reviewed by our team. Once approved, an employee will be assigned and you'll receive confirmation.",
  "status": "success",
  "timestamp": 1730812800000
}
```

**Error Response (400 Bad Request):**
```json
{
  "answer": "Please provide a question.",
  "status": "error",
  "timestamp": 1730812800000
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "answer": "I'm sorry, I encountered an error while processing your request. Please try again.",
  "status": "error",
  "timestamp": 1730812800000
}
```

### Health Check

**GET** `/api/chatbot/health`

Check if the chatbot service is running.

**Response (200 OK):**
```
Chatbot service is running
```

## Chatbot Capabilities

The chatbot is trained to help with:

1. **Appointment Scheduling**: Guide users on booking, rescheduling, or canceling appointments
2. **Service Information**: Provide details about available services (repair, maintenance, inspection, diagnostics, etc.)
3. **Vehicle Support**: Assist with queries about different vehicle types (cars, motorcycles, trucks, vans)
4. **Appointment Status**: Explain appointment statuses (PENDING, APPROVED, REJECTED, COMPLETED)
5. **Employee Assignment**: Explain how employees are assigned to appointments
6. **Time Slots**: Inform about booking procedures
7. **Service Instructions**: Help users provide proper service instructions
8. **System Navigation**: Guide users on how to use system features

## Example Questions

Here are some example questions you can ask the chatbot:

- "How do I book an appointment?"
- "What services do you offer for cars?"
- "How long does a typical maintenance service take?"
- "Can I reschedule my appointment?"
- "What does PENDING status mean?"
- "Do you service motorcycles?"
- "What information do I need to provide when booking?"
- "How do I cancel my appointment?"

## JavaScript Example

```javascript
const askChatbot = async (question, previousQuestions = []) => {
  try {
    const response = await fetch('http://localhost:8080/api/chatbot/ask', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        question: question,
        previousQuestions: previousQuestions
      }),
    });
    
    if (response.ok) {
      const data = await response.json();
      return data.answer;
    } else {
      throw new Error('Failed to get chatbot response');
    }
  } catch (error) {
    console.error('Chatbot error:', error);
    throw error;
  }
};

// Usage example
const answer = await askChatbot(
  "How do I book an appointment?",
  ["What services do you offer?"]
);
console.log(answer);
```

## React Example with Conversation History

```javascript
import { useState } from 'react';

function Chatbot() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const sendMessage = async () => {
    if (!input.trim()) return;

    // Add user message to chat
    const userMessage = { type: 'user', text: input };
    setMessages(prev => [...prev, userMessage]);

    // Get previous questions for context
    const previousQuestions = messages
      .filter(m => m.type === 'user')
      .map(m => m.text)
      .slice(-5); // Last 5 questions for context

    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/chatbot/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          question: input,
          previousQuestions: previousQuestions
        }),
      });

      const data = await response.json();
      
      // Add bot response to chat
      const botMessage = { type: 'bot', text: data.answer };
      setMessages(prev => [...prev, botMessage]);
      
    } catch (error) {
      console.error('Error:', error);
      const errorMessage = { 
        type: 'bot', 
        text: 'Sorry, I encountered an error. Please try again.' 
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setLoading(false);
      setInput('');
    }
  };

  return (
    <div className="chatbot">
      <div className="messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message ${msg.type}`}>
            {msg.text}
          </div>
        ))}
        {loading && <div className="loading">Thinking...</div>}
      </div>
      <div className="input-area">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Ask me anything..."
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
}
```

## Best Practices

1. **Context Management**: Include previous questions (up to 5-10) for better conversation flow
2. **Error Handling**: Always handle potential errors and provide user-friendly messages
3. **Loading States**: Show loading indicators while waiting for responses
4. **Rate Limiting**: Consider implementing rate limiting on the frontend to prevent spam
5. **Conversation Reset**: Provide option to clear conversation history
6. **Fallback**: Have a fallback message when the chatbot can't answer

## Notes for Frontend Developers

- No authentication required for chatbot endpoint
- Responses are generated in real-time (may take 2-5 seconds)
- The chatbot maintains context through `previousQuestions` array
- Maximum recommended previous questions: 10 (for optimal response time)
- The chatbot is focused on automobile service management topics
- For specific user data (appointments, etc.), users should use authenticated endpoints
