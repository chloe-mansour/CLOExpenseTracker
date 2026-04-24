# CLO - Expense Tracker

CLO is a JavaFX desktop application designed to help users manage their income, track expenses, and analyze financial habits through interactive charts and structured insights.

---

## Features

### Authentication
- User signup and login system
- Email format validation
- Strong password validation
- Password visibility toggle
- Session management

### Financial Management
- Add income and expenses
- Edit and delete transactions (full CRUD)
- Category-based expense tracking
- Support for notes, dates, and descriptions

### Dashboard
- Overview of total income, expenses, and balance
- Categorized wallet sections:
    - Essentials
    - Education & Work
    - Savings & Investments
    - Lifestyle & Fun
    - Health & Emergencies

### Insights and Analytics
- Pie chart for expense distribution
- Bar chart for income vs expenses
- Weekly, monthly, and yearly filters
- Basic financial advice based on user data

### Settings
- Dark mode with persistence
- Custom USD to LBP exchange rate

### User Experience
- Keyboard navigation using Enter key
- Input validation and error highlighting
- Clean and structured UI
- Confirmation dialogs for important actions

---

## Technologies Used

- Java
- JavaFX
- SQLite
- Maven
- CSS

---

## Project Structure
src/main/java/oopproject/

model -> Data classes (User, Expense, Income)
service -> Business logic and database handling
ui -> JavaFX views (application screens)
util -> Utility classes (SceneManager, ThemeManager, SessionManager)


---


---

## How to Run

1. Clone the repository:
```bash
git clone https://github.com/chloe-mansour/CLOExpenseTracker.git
```
2. Open the project in IntelliJ IDEA
3. Make sure Java (JDK 21 recommended) is configured
4. Run:
```bash
mvn clean javafx:run
```