plugins {
    java // Tell gradle this is a java project.
    id("io.github.goooler.shadow") version "8.1.8"
    eclipse // Import eclipse plugin for IDE integration.
}

java {
    // Declare java version.
    sourceCompatibility = JavaVersion.VERSION_17
}

group = "net.trueog.oldenchantog" // Declare bundle identifier.
version = "1.0" // Declare plugin version (will be in .jar).
val apiVersion = "1.19" // Declare minecraft server target version.

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion
    )

    inputs.properties(props) // Indicates to rerun if version changes.

    filesMatching("plugin.yml") {
        expand(props)
    }
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    
    implementation("com.github.cryptomorin:XSeries:8.6.1")
    
    testImplementation("junit:junit:3.8.1")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("com.cryptomorin.xseries", "my.plugin.utils")

    exclude("com/cryptomorin/xseries/XBiome*")
    exclude("com/cryptomorin/xseries/XBlock*")
    exclude("com/cryptomorin/xseries/XEnchantment*")
    exclude("com/cryptomorin/xseries/XEntity*")
    exclude("com/cryptomorin/xseries/XItemStack*")
    exclude("com/cryptomorin/xseries/XPotion*")
    exclude("com/cryptomorin/xseries/XSound*")
    exclude("com/cryptomorin/xseries/XTag*")
    exclude("com/cryptomorin/xseries/messages/**")
    exclude("com/cryptomorin/xseries/particles/**")
    exclude("com/cryptomorin/xseries/SkullUtils*")
    exclude("com/cryptomorin/xseries/NMSExtras*")
    exclude("com/cryptomorin/xseries/ReflectionUtils*")
    exclude("com/cryptomorin/xseries/NoteBlockMusic*")
    exclude("com/cryptomorin/xseries/SkullCacheListener*")
    
    exclude("io/github/miniplaceholders/**")

    minimize()
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("part")
}

tasks.shadowJar {
    archiveClassifier.set("") // Use empty string instead of null
    from("LICENSE") {
        into("/")
    }
}

tasks.jar {
    dependsOn("shadowJar")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
    options.isFork = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
