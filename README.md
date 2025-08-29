# GuessSongs - Android Music Guessing Game

## ⚠️ Project Status: Discontinued

**This project has been discontinued and is no longer maintained.**

## 📱 Project Overview

GuessSongs is an Android-based music guessing game application with the following key features:

- 🎵 Music playback and song guessing functionality
- 👥 User registration and login system
- 🏆 Competitive mode and leaderboards
- 📊 Game history tracking
- 🎮 Endless mode and classic mode
- 🎨 Modern user interface design

## 🛑 Discontinuation Reason

This project has been discontinued for the following reasons:

### Cloud Storage Service Expiration
- **Storage Service**: The project uses Alibaba Cloud OSS (Object Storage Service) to store song files
- **Service Period**: Only had a 3-month free trial account
- **Expiration Impact**: After OSS service expiration, the app cannot properly load and play songs
- **Maintenance Cost**: Long-term maintenance of cloud storage services requires ongoing financial investment

### Technical Architecture Dependencies
- Core application functionality depends on cloud-based song resources
- No local song library as a fallback option
- Cloud service unavailability causes complete application failure

## 🔧 Technology Stack

- **Development Language**: Java
- **Development Platform**: Android Studio
- **Backend Service**: Firebase
- **Cloud Storage**: Alibaba Cloud OSS
- **UI Framework**: Native Android Development

## 📁 Project Structure

```
GuessSongs/
├── app/
│   ├── src/main/java/com/example/guesssongs/
│   │   ├── Class/           # Data model classes
│   │   ├── competitiveMode/ # Competitive mode related
│   │   ├── log/            # Login and registration functionality
│   │   ├── nav/            # Navigation related
│   │   └── ...             # Other functional modules
│   └── src/main/res/       # Resource files
├── gradle/                 # Gradle configuration
└── README.md              # Project documentation
```

## 💡 Learning Value

Although the project has been discontinued, the code still holds educational value for:

1. **Android Development Practice**: Complete Android application development workflow
2. **Cloud Service Integration**: Examples of Firebase and Alibaba Cloud OSS usage
3. **UI Design**: Modern Android interface design
4. **Architecture Design**: Modular code organization structure
5. **Feature Implementation**: Music playback, user systems, game logic, etc.

## 📝 Important Notes

- This project is for learning and reference purposes only
- Due to cloud service expiration, the application cannot run normally
- To reactivate, new cloud storage services need to be configured
- Consider adding local song resources as a fallback option

## 📄 License

This project is for educational and research purposes only.

---

*Last updated: January 2025*
