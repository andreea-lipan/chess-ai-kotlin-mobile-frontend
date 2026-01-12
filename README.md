# Chess Position Detection App

An Android mobile application that captures chessboard images, allows users to select board corners, and detects chess positions using computer vision.

## Overview

This app enables users to take a photo of a physical chessboard, manually mark the four corners of the board, and get the chess position analyzed by a Python backend. The detected position is returned as FEN notation along with a shareable Lichess link.

## Features

- **Camera Integration**: Capture chessboard images directly from the camera
- **Corner Selection**: Corner marking with visual feedback
- **Position Detection**: Backend processing to detect chess piece positions
- **FEN Notation**: Get the board position in standard FEN format
- **Lichess Integration**: Clickable link to view/analyze position on Lichess
- **Visual Feedback**: Display detected position with corner markers

## Tech Stack

### Android (Kotlin + Jetpack Compose)

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Camera**: CameraX library
- **Image Loading**: Coil
- **Networking**: Retrofit + OkHttp
- **Architecture**: MVVM pattern
- **SDK tested on**: 36 (Android 16.0)

### Backend (Python)

- **Framework**: FastAPI
- **Image Processing**: OpenCV, PIL
- **Chess Logic**: python-chess
- **API Endpoint**: `/detect` (POST)

## Prerequisites

### Android Development
- Android Studio
- JDK 11 or higher
- Android SDK 24+

### Backend Server
- Python 3.8+
- Backend server running locally on port 8000

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/andreea-lipan/chess-ai-kotlin-mobile-frontend.git
```

### 2. Open in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned repository
4. Wait for Gradle sync to complete

### 3. Configure Backend URL

Update the base URL in `ChessDetectionService.kt`:

```kotlin
// By default it uses the deployed railway link
private const val BASE_URL = "https://chess-ai-python-backend-production.up.railway.app/"

// For Android Emulator (connects to localhost on host machine)
private const val BASE_URL = "http://localhost:8000/"

// For Physical Device (use your computer's IP on the same network)
private const val BASE_URL = "http://192.168.0.XXX:8000/"
```

### 4. Run the Backend Server

Ensure the Python backend is running locally (or use the deployed link above):

```bash
# Navigate to backend directory
# Install dependencies
pip install -r requirements.txt

# Run the server
uvicorn main:app --reload --port 8000
```

## Usage

### 1. Launch the App

- Open the app on your Android device or emulator
- Grant camera permissions in settings

### 2. Capture or Select Image

- Point camera at chessboard
- Tap the camera button to capture
- Make sure all chessboard corners are visible

### 3. Mark Board Corners

- Tap the four corners of the chessboard in this order:
    1. Top-Left
    2. Top-Right
    3. Bottom-Right
    4. Bottom-Left
- Visual markers (red/white circles) and green lines show your selections
- Use "Reset" button to clear selections if needed
- Tap "Confirm" when all 4 corners are marked
- Tap "Back" to go back to the camera screen and take another picture

### 4. View Results

The app will:
- Send the image and corner coordinates to the backend
- Display a loading indicator while processing
- Show the detected position with:
    - Lichess link (clickable)
    - FEN notation
    - Corner coordinates
    - Visual representation with marked corners

### 5. Retry or Analyze New Board

- Tap "Capture New Image" to analyze another chessboard

## API Specification

### Endpoint: POST `/detect`

**Request (multipart/form-data):**
```
- image: File (JPEG/PNG)
- corners: JSON string
  {
    "topLeft": {"x": float, "y": float},
    "topRight": {"x": float, "y": float},
    "bottomRight": {"x": float, "y": float},
    "bottomLeft": {"x": float, "y": float}
  }
- original_width: string (displayed image width)
- original_height: string (displayed image height)
```

**Response (JSON):**
```json
{
  "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  "lichess_url": "https://lichess.org/analysis/...",
  "board_matrix": [
    ["r", "n", "b", "q", "k", "b", "n", "r"],
    ["p", "p", "p", "p", "p", "p", "p", "p"],
    ...
  ]
}
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/yourpackage/
│   │   │   ├── view/
│   │   │   │   ├── CameraScreen.kt          # Camera capture
│   │   │   │   ├── CornerSelectionScreen.kt # Corner marking UI
│   │   │   │   └──ResultScreen.kt          # Results display
│   │   │   ├── service/
│   │   │   │   └──ChessDetectionService.kt  # Retrofit API interface + Data models
│   │   │   ├── MainActivity.kt              # App starting point
│   │   │   └── ChessboardAppRputer.kt       # App navigation
│   │   ├── res/                             # App icons                  
│   │   └── AndroidManifest.xml              # Android permissions and app info
│   └── build.gradle.kts
└── build.gradle.kts
```

## Key Components

### CameraScreen
- Handles camera preview and image capture

### CornerSelectionScreen
- Interactive screen for corner selection
- Visual feedback with markers and connecting lines
- Tracks displayed image dimensions for backend scaling

### ResultScreen
- Displays the image with the corners marked
- Displays analysis results
- Shows clickable Lichess link
- Renders image with scaled corner markers
- Provides retry functionality

### ChessDetectionService
- Manages API communication

## Future Enhancements

- Automatic corner detection using computer vision
- Save history of analyzed positions
- Support for different chess sets variants

---

**Note**: This app requires the backend server to function. Ensure the Python backend is properly configured and running before using the app.