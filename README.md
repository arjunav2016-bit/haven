# HAVEN

HAVEN is a Java Swing desktop application designed for students and property owners to discover, manage, and book student accommodations around engineering colleges. It provides multi-criteria property search, interactive map exploration, direct messaging, enquiry workflow tracking, and administration tools.

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Windows OS (for running the `.bat` convenience scripts)
- (Optional) MongoDB running on `localhost:27017` - if MongoDB is not present, the application automatically falls back to an offline file-backed data store (`data/haven_store.json`).

## Build & Run

### 1. Compile
To compile production and test sources:
```cmd
compile.bat
```

### 2. Run
To launch the application:
```cmd
run.bat
```

Default demo accounts are provided for quick login:
- **Student**: `student` / `pass123`
- **Owner**: `owner` / `pass123`
- **Admin**: `admin` / `admin123`

### 3. Run Tests
To run the automated test suite:
```cmd
test.bat
```
