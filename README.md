Gatling plugin for Maven - Java demo project
============================================

A simple showcase of a Maven project using the Gatling plugin for Maven. Refer to the plugin documentation
[on the Gatling website](https://gatling.io/docs/current/extensions/maven_plugin/) for usage.

This project is written in Java, others are available for [Kotlin](https://github.com/gatling/gatling-maven-plugin-demo-kotlin)
and [Scala](https://github.com/gatling/gatling-maven-plugin-demo-scala).

It includes:

* [Maven Wrapper](https://maven.apache.org/wrapper/), so that you can immediately run Maven with `./mvnw` without having
  to install it on your computer
* minimal `pom.xml`
* latest version of `io.gatling:gatling-maven-plugin` applied
* sample [Simulation](https://gatling.io/docs/gatling/reference/current/general/concepts/#simulation) class,
  demonstrating sufficient Gatling functionality
* proper source file layout




### Print response body

```java
	public static ChainBuilder registerPatient = exec(
			http("Generate OMRS Identifier")
					.post("/openmrs/ws/rest/v1/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier")
					.body(StringBody("{}"))
					.check(jsonPath("$.identifier").saveAs("identifier"))
					.check(bodyString().saveAs("responseBody")))
			.exec(session -> {
				String responseBody = session.getString("responseBody");
				System.out.println("Generate OMRS Identifier Response: " + responseBody);
				return session;
			});
```
