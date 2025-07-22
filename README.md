# LiftDrop

### Instituto Superior de Engenharia de Lisboa
#### Summer Semester 2024/2025


### Project Description

**LiftDrop** is an Android application developed to explore the technical challenges of implementing a delivery system. The project focuses on the courier experience while simulating client interactions through API calls.

### System Components

#### Courier Application
- 📱 Order management interface
- 🗺️ Integrated Google Maps navigation
- 🔒 Secure delivery confirmation (PIN-based)
- 🔄 Automatic order reassignment on cancellation
- 📍 Location tracking using Foreground Service

#### Client Simulation
- All client actions simulated via HTTP requests to: https://two025-lift-drop.onrender.com
- Complete [Postman collection](./docs/api/LiftDrop_Client_API_Collection.json) includes:
- `POST client/api/makeOrder` - Create delivery requests
- `GET client/api/getRequestStatus/{id}` - Get request status
- `POST client/api/giveClassification` - Give rating to a courier's delivery


### Project Organization

- [**Frontend folder**](./code/frontend) – Contains the **Android application** source code (**frontend**).
- [**Backend folder**](./code/backend) – Holds the **backend** code of the application (**server side**).
- [**Docs folder**](./docs) – Contains **documentation** relevant for understanding the application.

## Setup Instructions

### Prerequisites
- ✔ [Git](https://git-scm.com/downloads) (for repository cloning)
- ✔ [Android Studio](https://developer.android.com/studio) (for mobile app)
- ✔ [Postman](https://www.postman.com/) (for client requests)


### Running the Project

1. **Frontend (Android)**
   ```bash
   # git clone https://github.com/isel-sw-projects/2025-lift-drop
   # cd code/frontend/LiftDrop
   # Open in Android Studio
   # Run on emulator or physical device
   # Enable USB debugging if using real device (recommended)
   
2. **Backend**
   
   - Already deployed at https://two025-lift-drop.onrender.com
   - Allow 20-30s for cold start if instance is inactive


## Technologies
## Frontend

![](https://skillicons.dev/icons?i=kotlin,androidstudio)

## Backend
![](https://skillicons.dev/icons?i=kotlin,spring,postgresql,gcp,docker)

## Developers

- [João Ramos](https://github.com/JohnnyR2003)
- [Gonçalo Morais](https://github.com/Goncalo-Morais)

## Supervisors

- [Miguel Gamboa](https://github.com/fmcarvalho)
- [Diogo Silva](https://www.linkedin.com/in/diogofdsilva/)
