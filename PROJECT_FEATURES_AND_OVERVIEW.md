# TrekSathi - Complete System Features and Overview

## 🎯 Project Overview

**TrekSathi** is a comprehensive **Hike and Trek Management System** designed to connect trekking enthusiasts with event organizers in Nepal. It serves as a complete platform for discovering, booking, and managing trekking events, with integrated payment processing, real-time communication, and AI-powered assistance.

---

## 🔑 Core Problems Solved

1. **Fragmented Trek Discovery**: Provides a centralized platform where users can discover all available trekking events in Nepal
2. **Payment Processing Challenges**: Integrates multiple payment gateways (eSewa, Stripe) for seamless transaction processing
3. **Communication Gap**: Enables real-time communication between organizers and participants through chat rooms
4. **Event Management Complexity**: Simplifies event creation, registration, and participant management for organizers
5. **Lack of Trust & Reviews**: Implements a comprehensive review and rating system to build trust
6. **Information Overload**: AI-powered chatbot helps users find relevant treks based on natural language queries
7. **Manual Booking Processes**: Automates event registration, payment verification, and booking confirmations
8. **Revenue Tracking**: Provides organizers with detailed payment dashboards and analytics

---

## 👥 User Roles

1. **Hiker/User**: Browse events, register, make payments, chat, review, and manage bookings
2. **Organizer**: Create events, manage participants, track payments, send notifications
3. **Admin**: System administration and oversight

---

## 🚀 Complete Feature List

### 1. **User Authentication & Authorization**

#### Features:
- **User Registration**: Email-based registration with OTP verification
- **User Login**: JWT-based authentication with refresh token support
- **OAuth2 Integration**: Social login support (Google, etc.)
- **OTP Verification**: Email OTP for account verification
- **Resend OTP**: Ability to resend verification codes
- **Token Management**: JWT access tokens and refresh tokens
- **Token Blacklisting**: Secure logout with token invalidation
- **Profile Image Management**: Upload and retrieve user profile images
- **Account Status Management**: ACTIVE, INACTIVE, SUSPENDED status tracking

#### Security Features:
- JWT-based authentication
- Token expiration handling
- Secure password storage
- Role-based access control (RBAC)

---

### 2. **Organizer Management**

#### Features:
- **Organizer Registration**: Separate registration flow for event organizers
- **Organizer Profile Management**: 
  - View organizer profile
  - Update organizer profile (organization name, contact details, etc.)
- **Organizer Dashboard**: 
  - Overview of events, participants, and revenue
  - Statistics and analytics
- **Organizer Search**: Search functionality to find organizers by name

---

### 3. **Event Management**

#### For Organizers:
- **Event Creation**: Create new trekking events with comprehensive details:
  - Title, description, location
  - Date, duration, meeting point and time
  - Difficulty level (EASY, MODERATE, HARD, EXTREME)
  - Price, max participants
  - Banner images
  - Included services and requirements
- **Event Update**: Modify existing event details
- **Event Status Management**: 
  - Update event status (PENDING, ACTIVE, COMPLETED, CANCELLED)
  - Filter events by status
- **Event Deletion**: Remove events from the system
- **Event Details View**: Comprehensive view of all event information
- **Bulk Email to Participants**: Send emails with attachments to all event participants

#### For Users:
- **Event Browsing**: 
  - View all events with pagination
  - View event details by ID
  - Event cards with key information
- **Event Search**: Advanced search with multiple filters
- **Event Filtering**: Filter by status, difficulty, price range, dates, location

---

### 4. **Event Registration & Booking**

#### Features:
- **Event Registration**: Register for trekking events
- **Registration Details**: View complete registration information
- **Registration Status Tracking**: 
  - SUCCESS (confirmed)
  - CANCEL (cancelled)
- **User Bookings**: View all bookings by user ID with status filtering
- **Upcoming Events**: Get list of upcoming events for a user
- **Booking Confirmation**: Automatic confirmation after successful payment
- **Registration Cancellation**: Organizers can cancel event registrations

---

### 5. **Payment System**

#### Payment Gateways Integrated:
1. **eSewa Payment Gateway** (Nepal's popular payment method)
   - Payment initiation
   - Payment verification
   - Success/failure callbacks
   - Signature verification for security

2. **Stripe Payment Gateway** (International payments)
   - Checkout session creation
   - Payment verification
   - Support for card payments

#### Payment Features:
- **Payment Initiation**: Start payment process for event registration
- **Payment Verification**: Verify and confirm payments
- **Payment Status Tracking**: 
  - PENDING
  - COMPLETED
  - FAILED
  - REFUNDED
- **Payment Methods**: CARD, ESEWA, STRIPE
- **Transaction Management**: Track all transactions with unique UUIDs
- **Payment History**: View payment history for users and organizers

#### Organizer Payment Dashboard:
- **Payment Analytics**: 
  - Total earnings
  - Payment summary by status
  - Revenue charts (last 6 months)
  - Monthly growth calculations
- **Payment Filtering**:
  - Filter by date range
  - Filter by payment status
  - Filter by event ID
  - Filter by payment method
- **Event-wise Payments**: View payments grouped by event
- **Participant Payments**: View payments grouped by participant
- **Revenue Trends**: Visual representation of revenue over time

---

### 6. **AI-Powered Chatbot (RAG-based)**

#### Features:
- **Natural Language Queries**: Ask questions about treks in natural language
- **Event Discovery**: AI finds relevant events based on user queries
- **Contextual Responses**: RAG (Retrieval Augmented Generation) for accurate answers
- **Event Cards in Response**: Returns matching events as cards with details
- **Search with Filters**: 
  - Location-based search
  - Difficulty level filtering
  - Date range filtering
- **Trending Events**: Get popular/trending events
- **Personalized Recommendations**: Get event recommendations based on preferences
- **Vector Store Integration**: Uses Qdrant vector store for semantic search

#### Example Queries:
- "Show me treks in January"
- "I want moderate difficulty treks near Kathmandu"
- "What treks are available in Everest region?"

---

### 7. **Advanced Search System**

#### Search Features:
- **Multi-criteria Search**:
  - Text query (title, description, location)
  - Difficulty level
  - Event status
  - Price range (min/max)
  - Date range (start/end dates)
  - Duration range (min/max days)
  - Location
  - Organizer name/ID
- **Pagination**: Paginated search results
- **Sorting**: Sort by date, price, etc. (ASC/DESC)
- **Quick Suggestions**: Autocomplete suggestions for search queries
- **Popular Locations**: Get list of popular trekking locations
- **Organizer Search**: Search for organizers by name

---

### 8. **Real-Time Chat System**

#### Features:
- **WebSocket Integration**: Real-time messaging using WebSocket
- **Chat Rooms**: 
  - Event-based chat rooms
  - Group chat functionality
  - Private chat support
- **Chat Room Types**: Different room types (EVENT, PRIVATE, GROUP)
- **Message Sending**: Send messages in real-time
- **Message History**: Retrieve chat message history
- **Participant Management**: 
  - Auto-enroll event participants in chat rooms
  - Manual chat room creation
- **User Chat Rooms**: View all chat rooms for a user
- **Organizer Chat Rooms**: View chat rooms for specific events
- **Read Receipts**: Track message read status
- **Presence Indicators**: User join/leave notifications

---

### 9. **Review & Rating System**

#### Features:
- **Review Submission**: Submit reviews for completed events
- **Rating System**: Rate events (typically 1-5 stars)
- **Review Management**:
  - View my reviews
  - Update existing reviews
  - Delete reviews
- **Pending Reviews**: 
  - View events eligible for review
  - Events completed within last 30 days
  - Shows days until review expiry
- **Review Display**: Reviews shown on event details
- **Review Validation**: 
  - Only completed events can be reviewed
  - 30-day window for reviews
  - One review per user per event

---

### 10. **Favorites/Wishlist System**

#### Features:
- **Add to Favorites**: Save events to favorites list
- **Remove from Favorites**: Remove events from favorites
- **Toggle Favorite**: Smart add/remove functionality
- **Check Favorite Status**: Verify if event is favorited
- **View Favorites**: Paginated list of favorite events
- **Favorites Count**: Get total number of favorited events
- **Quick Lookup**: Get list of favorited event IDs

---

### 11. **Notification System**

#### Features:
- **Real-time Notifications**: Send notifications to users
- **Notification Types**:
  - Booking confirmations
  - Event updates
  - Payment confirmations
  - General notifications
- **Notification Management**:
  - View all notifications (paginated)
  - Mark as read
  - Mark all as read
  - Delete notifications
- **Unread Count**: Track unread notification count
- **Broadcast Notifications**: Send notifications to multiple users
- **Event-based Notifications**: Automatic notifications for booking confirmations
- **Reference Tracking**: Link notifications to events, registrations, etc.

---

### 12. **Hiker Dashboard**

#### Features:
- **Dashboard Overview**: Comprehensive dashboard for hikers
- **Statistics**:
  - Total bookings
  - Upcoming events count
  - Completed events count
  - Total spent
- **Upcoming Adventures**: List of upcoming registered events
- **Recommended Events**: AI-powered event recommendations
- **Recent Activity**: Recent bookings, reviews, favorites
- **Quick Actions**: Quick access to common features

---

### 13. **Participant Management**

#### Features:
- **Participant Registration**: Register participants for events
- **Participant List**: View all participants for an event
- **Attendance Management**: 
  - Mark attendance for participants
  - Bulk attendance marking
- **Participant Details**: View detailed participant information
- **Participant Status**: Track participant status

---

### 14. **Event Status Workflow**

#### Event Statuses:
- **PENDING**: Event awaiting approval
- **ACTIVE**: Event is live and accepting registrations
- **COMPLETED**: Event has finished
- **CANCELLED**: Event has been cancelled

#### Status Transitions:
- Organizers can update event status
- Admin can approve/reject events
- Automatic status updates based on dates

---

### 15. **Data Management & Analytics**

#### Features:
- **Payment Analytics**: Revenue tracking and trends
- **Event Analytics**: Event performance metrics
- **User Analytics**: User engagement statistics
- **Dashboard Data**: Aggregated data for dashboards
- **Reporting**: Generate reports for organizers

---

## 🛠️ Technical Features

### 1. **API Documentation**
- **OpenAPI/Swagger**: Complete API documentation
- **Tagged Endpoints**: Organized by feature areas
- **Security Schemes**: JWT authentication documentation

### 2. **Database Management**
- **JPA/Hibernate**: Object-relational mapping
- **Entity Relationships**: Complex relationships between entities
- **Transaction Management**: ACID compliance
- **Query Optimization**: Efficient database queries

### 3. **Security**
- **JWT Authentication**: Secure token-based auth
- **Password Encryption**: Secure password storage
- **CORS Configuration**: Cross-origin resource sharing
- **Input Validation**: Request validation
- **Exception Handling**: Global exception handling

### 4. **Real-time Features**
- **WebSocket**: Real-time chat
- **STOMP Protocol**: Messaging protocol
- **Async Processing**: Asynchronous task processing

### 5. **File Management**
- **Image Upload**: Profile and banner image handling
- **URL Storage**: Cloud storage integration

### 6. **Email Services**
- **OTP Emails**: Verification code emails
- **Bulk Emails**: Mass email to participants
- **Email Attachments**: Support for file attachments

### 7. **Vector Search**
- **Qdrant Integration**: Vector database for AI search
- **Semantic Search**: Meaning-based search
- **Embeddings**: Text embedding for similarity search

---

## 📊 System Architecture Highlights

1. **Layered Architecture**: Controller → Service → Repository
2. **Interface-based Design**: Loose coupling with interfaces
3. **DTO Pattern**: Data Transfer Objects for API communication
4. **Record Classes**: Immutable data structures
5. **Mapper Pattern**: Entity to DTO conversion
6. **Specification Pattern**: Dynamic query building
7. **Exception Handling**: Centralized exception management
8. **Configuration Management**: Environment-based configuration

---

## 🔄 Workflows

### Event Registration Workflow:
1. User browses/searches events
2. User selects event and fills registration form
3. System initiates payment (eSewa/Stripe)
4. User completes payment
5. System verifies payment
6. Event registration confirmed
7. Notifications sent to user and organizer
8. User enrolled in event chat room (if applicable)

### Event Creation Workflow:
1. Organizer creates event with details
2. Event saved with PENDING status
3. Admin approves (or auto-approved)
4. Event becomes ACTIVE
5. Users can discover and register
6. Organizer manages participants
7. Event occurs
8. Status changes to COMPLETED
9. Users can submit reviews

---

## 📈 Key Metrics & Statistics

### For Organizers:
- Total events created
- Total participants
- Total revenue
- Payment breakdown by method
- Revenue trends (monthly)
- Event performance

### For Users:
- Total bookings
- Upcoming events
- Completed events
- Total spent
- Reviews submitted
- Favorites count

---

## 🎨 User Experience Features

1. **Pagination**: Efficient data loading
2. **Filtering**: Multiple filter options
3. **Sorting**: Flexible sorting capabilities
4. **Search**: Quick and advanced search
5. **Responsive Design**: Mobile-friendly (frontend)
6. **Real-time Updates**: Live chat and notifications
7. **Error Handling**: User-friendly error messages
8. **Loading States**: Better UX during async operations

---

## 🔐 Security & Compliance

1. **Authentication**: JWT-based secure authentication
2. **Authorization**: Role-based access control
3. **Data Validation**: Input validation and sanitization
4. **Payment Security**: Secure payment gateway integration
5. **Token Management**: Secure token handling and blacklisting
6. **HTTPS**: Secure communication (in production)
7. **Error Handling**: Secure error messages (no sensitive data exposure)

---

## 📱 Integration Points

1. **Payment Gateways**: eSewa, Stripe
2. **Email Service**: Email sending for OTP and notifications
3. **Vector Database**: Qdrant for AI search
4. **AI/ML Service**: Spring AI for chatbot
5. **OAuth2 Providers**: Google, etc.
6. **File Storage**: Cloud storage for images

---

## 🚀 Future Enhancement Areas (from test documentation)

1. Performance tests for large datasets
2. Integration tests with real database
3. Contract tests for API endpoints
4. Mutation testing
5. Enhanced test coverage reports

---

## 📝 Summary

**TrekSathi** is a full-featured trekking event management platform that solves real-world problems in the Nepalese trekking industry. It provides:

- **For Users**: Easy discovery, booking, payment, and communication
- **For Organizers**: Complete event management, participant tracking, and revenue analytics
- **For the Industry**: Centralized platform, trust building through reviews, and streamlined operations

The system leverages modern technologies including AI, real-time communication, multiple payment gateways, and comprehensive analytics to deliver a complete solution for trekking event management.

