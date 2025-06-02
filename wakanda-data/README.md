# 🌟 Wakanda Social Database

This folder contains all your Wakanda Social application data.

## 📁 Files:
- `wakanda-social-db.mv.db` - Main database file (contains all user data, posts, comments, etc.)
- `wakanda-social-db.trace.db` - Database trace file (for debugging)

## 🔐 Database Access:
- **JDBC URL**: `jdbc:h2:file:./wakanda-data/wakanda-social-db`
- **Username**: `wakanda_admin`
- **Password**: `vibranium_secure_2024`
- **Web Console**: `http://localhost:8080/h2-console`

## 🛡️ Security:
- All user passwords are encrypted using BCrypt
- JWT tokens are used for secure authentication
- Database is file-based and persistent

## 📊 Data Persistence:
- All data is automatically saved to this folder
- Data persists between application restarts
- Backup this folder to preserve all user data

## 🚀 Demo Users:
- alice / password123 (Guardian of Knowledge)
- bob / password123 (Tech Innovator)
- charlie / password123 (Positivity Spreader)
- diana / password123 (Warrior Princess)
- erik / password123 (Revolutionary Thinker)

## 🚀 Created by Wakanda Social v1.0
