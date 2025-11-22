# **Frontend API Documentation**

This document provides API-level documentation for the primary frontend components of the CyMarket Android application. It details the Activities responsible for user authentication and item purchasing, including their responsibilities, input/output behavior, and network API interactions.

# **1. LoginActivity**

---

## **Class Summary**

`LoginActivity` handles user login.
It takes a user’s email and password, sends them to the backend authentication API, and stores the session token on success.

---

## **Responsibilities**

* Render login UI
* Validate user input
* Send a POST request to the backend login endpoint
* Save authentication token in SharedPreferences
* Navigate to BuyActivity on success
* Provide navigation to SignupActivity

---

## **Fields**

| Field                       | Type       | Description             |
| --------------------------- | ---------- | ----------------------- |
| `EditText emailEditText`    | UI input   | Email field             |
| `EditText passwordEditText` | UI input   | Password field          |
| `Button loginButton`        | UI control | Starts login            |
| `TextView signupButton`     | UI control | Navigate to signup      |
| `TextView forgotPassword`   | UI control | (Optional UI element)   |
| `TextView secretAdmin`      | UI control | (Optional admin access) |

---

## **Public Methods**

### **`onCreate(Bundle savedInstanceState)`**

Initializes UI references, sets listeners for buttons, and prepares network request logic.

---

### **`attemptLogin()`**

**Input:** User email + password (via EditTexts)
**Process:**

* Validates fields
* Builds JSON with credentials
* Sends Volley POST request
  **Output:**
* On success → navigates to `BuyActivity`
* On failure → displays Toast error message

---

## **API Interaction**

### **Endpoint:**

```
POST /api/auth/login
```

### **Request Body:**

```json
{
  "email": "string",
  "password": "string"
}
```

### **Success Response:**

```json
{
  "token": "jwt-string",
  "user_id": int
}
```

### **Failure Response:**

```json
{
  "error": "Invalid credentials"
}
```

Token is saved in `SharedPreferences` for authenticated requests.

---

# **2. SignupActivity**

---

## **Class Summary**

`SignupActivity` handles new user registration.
It collects personal information, performs validation, calls the backend signup API, and redirects users to LoginActivity when completed.

---

## **Responsibilities**

* Render signup UI
* Validate first name, last name, email, username, password
* Ensure password confirmation matches
* Send registration data to backend
* Handle success/failure
* Navigate back to LoginActivity

---

## **Fields**

| Field                       | Type       | Description          |
| --------------------------- | ---------- | -------------------- |
| `EditText firstNameText`    | UI input   |                      |
| `EditText lastNameText`     | UI input   |                      |
| `EditText emailEditText`    | UI input   |                      |
| `EditText usernameEditText` | UI input   |                      |
| `EditText passwordEditText` | UI input   |                      |
| `EditText confirmEditText`  | UI input   |                      |
| `TextView loginText`        | UI control | Navigates to login   |
| `Button signupButton`       | UI control | Sends signup request |

---

## **Public Methods**

### **`onCreate(Bundle savedInstanceState)`**

Initializes UI components and assigns click listeners.

---

### **`attemptSignup()`**

**Input:**

* First name
* Last name
* Email
* Username
* Password
* Password confirmation

**Process:**

* Validates fields
* Builds JSON with new user info
* Sends Volley POST to backend signup endpoint

**Output:**

* On success → Toast + redirect to LoginActivity
* On error → show error message

---

## **API Interaction**

### **Endpoint:**

```
POST /api/auth/signup
```

### **Request Body:**

```json
{
  "first_name": "string",
  "last_name": "string",
  "email": "string",
  "username": "string",
  "password": "string"
}
```

### **Success Response:**

```json
{
  "message": "User created successfully"
}
```

### **Failure Response:**

```json
{
  "error": "Email already exists"
}
```

---

# **3. BuyActivity**

---

## **Class Summary**

`BuyActivity` retrieves product listings from the backend and displays them in a RecyclerView.
Users can view items and purchase them through additional API calls.

---

## **Responsibilities**

* Fetch marketplace listing data
* Display items using a RecyclerView
* Handle item purchase events
* Provide navigation back to LoginActivity
* Provide a clear button (possibly to wipe fields or refresh list)

---

## **Fields**

| Field                       | Type         | Description                       |
| --------------------------- | ------------ | --------------------------------- |
| `RecyclerView recyclerView` | UI component | Displays listings                 |
| `ListingAdapter adapter`    | UI adapter   | Binds backend data to UI          |
| `List<Listing> listingList` | Data         | Holds listings retrieved from API |
| `Button backButton`         | UI control   |                                   |
| `Button clearButton`        | UI control   |                                   |

---

## **Public Methods**

### **`onCreate(Bundle savedInstanceState)`**

* Sets up RecyclerView
* Calls a method to fetch listings
* Assigns listeners to control buttons

---

### **`fetchListings()`**

**Process:**

* Sends Volley GET request
* Parses the JSON array
* Converts each object into a `Listing` model
* Updates RecyclerView

**Output:**

* Updated UI
* Toast error if API request fails

---

### **`purchaseItem(int itemId)`** *(if implemented)*

**Endpoint:** POST `/api/purchase/{itemId}`
Sends a purchase request with the user’s token.

---

## **API Interaction**

### **Listing Retrieval Endpoint:**

```
GET /api/listings
```

### **Success Response:**

```json
[
  {
    "id": 1,
    "title": "Iowa State Hoodie",
    "price": 25.00,
    "seller": "user123"
  }
]
```

### **Purchase Endpoint (optional):**

```
POST /api/purchase/{id}
```

---


# **4. Application Flow Summary**

---

```
SignupActivity → (Successful signup) → LoginActivity
LoginActivity → (Valid login) → BuyActivity
BuyActivity → (Back button) → LoginActivity
```