# Carpooling Application - Angular Frontend

A modern carpooling (covoiturage) application built with Angular, following Clean Architecture principles.

## Features

- 🔐 Authentication (Login, Signup, Role Selection)
- 👥 Passenger Interface (Search Rides, Book Rides, My Bookings)
- 🚗 Driver Interface (Publish Rides, Manage Bookings)
- ⭐ Reviews & Ratings System
- 📊 Admin Dashboard
- 🚨 Reports Management
- 🔔 Notifications System

## Tech Stack

- Angular 17+ (Standalone Components)
- TailwindCSS for styling
- Signals for state management
- Clean Architecture structure

## Getting Started

```bash
# Install dependencies
npm install

# Run development server
npm start

# Build for production
npm run build
```

## Project Structure

```
src/
├── app/
│   ├── core/           # Core services, guards, interceptors, models
│   ├── shared/         # Shared UI components, directives, pipes
│   ├── features/       # Feature modules
│   └── app-routing.module.ts
└── assets/
```

## Architecture

The application follows Clean Architecture principles with:
- **Core**: Business logic, services, guards
- **Shared**: Reusable UI components
- **Features**: Domain-specific features

