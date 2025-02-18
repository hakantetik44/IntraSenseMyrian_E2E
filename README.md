# 🌟 Tests E2E Intrasense

<div align="center">
  <img src="https://img.shields.io/badge/Selenium-43B02A?style=for-the-badge&logo=selenium&logoColor=white"/>
  <img src="https://img.shields.io/badge/Cucumber-23D96C?style=for-the-badge&logo=cucumber&logoColor=white"/>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"/>
  <img src="https://img.shields.io/badge/Allure-C70D2C?style=for-the-badge&logo=allure&logoColor=white"/>
</div>

## 📝 Description

Ce projet contient les tests automatisés end-to-end pour le site web Intrasense, développés avec Selenium WebDriver, Cucumber et Java. Le framework implémente le modèle Page Object et inclut des rapports détaillés avec Allure, y compris des enregistrements vidéo des tests.

## 🔗 Repository GitHub

```bash
git clone https://github.com/hakantetik44/IntraSenseMyrian_E2E.git
```

## 🛠️ Technologies Utilisées

- Java 17
- Selenium WebDriver 4.18.1
- Cucumber 7.15.0
- Allure Reports 2.25.0
- Jenkins Pipeline
- Maven

## 🏗️ Structure du Projet

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

## 🚀 Fonctionnalités

- ✨ Tests automatisés de navigation sur le site Intrasense
- 📱 Vérification de la présence des éléments clés
- 🎥 Enregistrement vidéo des exécutions de test
- 📊 Rapports détaillés avec Allure
- 🔄 Intégration continue avec Jenkins
- 🎯 Architecture Page Object Model

## 📋 Scénarios de Test

Le projet inclut les scénarios de test suivants :
1. Navigation vers la page d'accueil
2. Accès à la section "Nos Solutions"
3. Vérification de la page "Découvrir Myrian"
4. Validation des sections "Plateforme Myrian" et "Les avantages Myrian"

## ⚙️ Prérequis

- Java JDK 17
- Maven
- Chrome Browser
- Allure Command Line Tool (pour les rapports)

## 🚀 Installation et Exécution

1. Cloner le repository :
```bash
git clone [URL_DU_REPO]
cd intrasense-e2e-tests
```

2. Installer les dépendances :
```bash
mvn clean install
```

3. Exécuter les tests :
```bash
mvn clean test
```

4. Générer et ouvrir le rapport Allure :
```bash
mvn allure:serve
```

## 📊 Rapports

Les rapports sont générés dans les formats suivants :
- Rapport Allure : `target/allure-results`
- Rapport Cucumber : `target/cucumber-reports`
- Enregistrements vidéo : `target/videos`

## 🔄 Pipeline Jenkins

Le projet inclut un pipeline Jenkins configuré qui :
1. Initialise l'environnement
2. Exécute les tests
3. Génère les rapports
4. Archive les résultats
5. Intègre avec Xray pour la gestion des tests

## 🤝 Contribution

Pour contribuer au projet :
1. Créer une branche (`git checkout -b feature/AmazingFeature`)
2. Commit des changements (`git commit -m 'Add some AmazingFeature'`)
3. Push vers la branche (`git push origin feature/AmazingFeature`)
4. Ouvrir une Pull Request

## 📝 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 👥 Contact

- **Équipe QA Intrasense**
- Email : contact@intrasense.fr
- Site Web : https://intrasense.fr/

## ⚙️ Configuration

Le projet utilise un fichier de configuration (`configuration.properties`) pour gérer les paramètres de test :

```properties
# Configuration du navigateur
browser=chrome
headless=false

# Configuration de l'URL
base_url=https://intrasense.fr/fr/

# Configuration des timeouts
implicit_wait=10
page_load_timeout=30

# Configuration des chemins
screenshot_path=target/screenshots/
video_path=target/videos/
report_path=target/allure-results/
```

---
<div align="center">
  <sub>Built with ❤️ by Intrasense QA Team</sub>
</div> # IntraSenseMyrian_E2E
