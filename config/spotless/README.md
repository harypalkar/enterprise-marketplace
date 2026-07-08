# Spotless configuration is defined in the parent pom.xml
# Run: mvn spotless:apply (format) or mvn spotless:check (verify)

googleJavaFormat:
  version: "1.25.2"
  style: AOSP

importOrder:
  - java
  - javax
  - jakarta
  - org
  - com
