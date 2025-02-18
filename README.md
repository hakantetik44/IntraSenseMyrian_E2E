# 🌟 Intrasense E2E Tests

<div align="center">
  <img src="https://img.shields.io/badge/Selenium-43B02A?style=for-the-badge&logo=selenium&logoColor=white"/>
  <img src="https://img.shields.io/badge/Cucumber-23D96C?style=for-the-badge&logo=cucumber&logoColor=white"/>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"/>
  <img src="https://img.shields.io/badge/Allure-C70D2C?style=for-the-badge&logo=allure&logoColor=white"/>
</div>

## 📝 Description

This project contains end-to-end automated tests for the Intrasense website, developed using Selenium WebDriver, Cucumber, and Java. The framework implements the Page Object Model and includes detailed reporting with Allure, including video recordings of test executions.

## 🔗 GitHub Repository

```bash
git clone https://github.com/hakantetik44/IntraSenseMyrian_E2E.git
```

## 🛠️ Technologies Used

- Java 17
- Selenium WebDriver 4.18.1
- Cucumber 7.15.0
- Allure Reports 2.25.0
- Jenkins Pipeline
- Maven

## 🏗️ Project Structure

```
├── src
│   ├── main
│   │   └── java
│   │       └── fr/intrasense
│   │           ├── pages
│   │           │   ├── BasePage.java
│   │           │   ├── HomePage.java
│   │           │   └── IntrasenseLocators.java
│   │           └── utils
│   │               ├── ConfigReader.java
│   │               └── DriverManager.java
│   └── test
│       ├── java
│       │   └── fr/intrasense/steps
│       │       ├── IntrasenseSteps.java
│       │       └── TestRunner.java
│       └── resources
│           ├── features
│           │   └── intrasense.feature
│           └── configuration.properties
├── pom.xml
├── Jenkinsfile
└── README.md
```

## 🚀 Features

- ✨ Automated navigation tests for Intrasense website
- 📱 Verification of key elements
- 🎥 Video recording of test executions
- 📊 Detailed reporting with Allure
- 🔄 Continuous integration with Jenkins
- 🎯 Page Object Model architecture

## 📋 Test Scenarios

The project includes the following test scenarios:
1. Navigation to homepage
2. Access to "Our Solutions" section
3. Verification of "Discover Myrian" page
4. Validation of "Myrian Platform" and "Myrian Advantages" sections

## ⚙️ Prerequisites

- Java JDK 17
- Maven
- Chrome Browser
- Allure Command Line Tool (for reports)

## 🚀 Installation and Execution

1. Clone the repository:
```bash
git clone [REPO_URL]
cd intrasense-e2e-tests
```

2. Install dependencies:
```bash
mvn clean install
```

3. Run tests:
```bash
mvn clean test
```

4. Generate and open Allure report:
```bash
mvn allure:serve
```

## 📊 Reports

Reports are generated in the following formats:
- Allure Report: `target/allure-results`
- Cucumber Report: `target/cucumber-reports`
- Video Recordings: `target/videos`

## 🔄 Jenkins Pipeline

The project includes a configured Jenkins pipeline that:
1. Initializes the environment
2. Executes tests
3. Generates reports
4. Archives results
5. Integrates with Xray for test management

## 🤝 Contributing

To contribute to the project:
1. Create a branch (`git checkout -b feature/AmazingFeature`)
2. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
3. Push to the branch (`git push origin feature/AmazingFeature`)
4. Open a Pull Request

## 📝 License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## 👥 Contact

- **Intrasense QA Team**
- Email: contact@intrasense.fr
- Website: https://intrasense.fr/

## ⚙️ Configuration

The project uses a configuration file (`configuration.properties`) to manage test parameters:

```properties
# Browser Configuration
browser=chrome
headless=false

# URL Configuration
base_url=https://intrasense.fr/fr/

# Timeout Configuration
implicit_wait=10
page_load_timeout=30

# Path Configuration
screenshot_path=target/screenshots/
video_path=target/videos/
report_path=target/allure-results/
```

---
<div align="center">
  <sub>Built with ❤️ by Intrasense QA Team</sub>
</div>
