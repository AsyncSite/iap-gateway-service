import org.gradle.kotlin.dsl.register

val serviceName = project.name
val composeFile = "docker-compose.${serviceName}-only.yml"

tasks.register("dockerBuildImage") {
    group = "docker"
    description = "Build Docker image for $serviceName"
    dependsOn("bootJar")
    doLast {
        exec {
            commandLine("docker", "build", "-t", "asyncsite/$serviceName:latest", ".")
        }
    }
}

tasks.register("dockerRebuildAndRun${serviceName.split("-").joinToString("") { it.replaceFirstChar(Char::titlecase) }}Only") {
    group = "docker"
    description = "Rebuild and run only this service via docker-compose"
    dependsOn("test", "bootJar", "dockerBuildImage")
    doLast {
        // compose file at project root
        exec {
            commandLine("docker", "compose", "-f", composeFile, "down")
            isIgnoreExitValue = true
        }
        exec {
            commandLine("docker", "compose", "-f", composeFile, "up", "-d")
        }
    }
}

tasks.register("dockerQuickRebuild${serviceName.split("-").joinToString("") { it.replaceFirstChar(Char::titlecase) }}Only") {
    group = "docker"
    description = "Quick rebuild (no tests) and up via docker-compose"
    dependsOn("bootJar")
    doLast {
        exec {
            commandLine("docker", "compose", "-f", composeFile, "up", "-d", "--build")
        }
    }
}

tasks.register("dockerLogs") {
    group = "docker"
    description = "Tail logs of container"
    doLast {
        exec {
            commandLine("docker", "logs", "-f", "asyncsite-$serviceName")
        }
    }
}
