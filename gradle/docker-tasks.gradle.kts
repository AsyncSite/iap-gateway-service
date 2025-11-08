import org.gradle.kotlin.dsl.register
import java.io.ByteArrayOutputStream

// Docker 실행 파일 찾기
fun findDockerExecutable(): String? {
    val possiblePaths = listOf(
        "/usr/local/bin/docker",
        "/usr/bin/docker",
        "/opt/homebrew/bin/docker",
        "/Applications/Docker.app/Contents/Resources/bin/docker"
    )

    for (path in possiblePaths) {
        if (file(path).exists()) {
            return path
        }
    }

    // which 명령어로 docker 찾기
    try {
        val output = ByteArrayOutputStream()
        exec {
            commandLine("which", "docker")
            standardOutput = output
            isIgnoreExitValue = true
        }
        val dockerPath = output.toString().trim()
        if (dockerPath.isNotEmpty() && file(dockerPath).exists()) {
            return dockerPath
        }
    } catch (e: Exception) {
        // ignore
    }

    return null
}

// Docker 명령어 실행 헬퍼 함수
fun executeDockerCommand(vararg args: String): Boolean {
    val dockerPath = findDockerExecutable()
    if (dockerPath == null) {
        println("❌ Docker not found. Please make sure Docker Desktop is installed and running.")
        return false
    }

    return try {
        val output = ByteArrayOutputStream()
        val result = exec {
            val fullCommand = listOf(dockerPath) + args.toList()
            commandLine(*fullCommand.toTypedArray())
            workingDir = projectDir
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }

        if (result.exitValue != 0) {
            println("Command failed: ${args.joinToString(" ")}")
            println(output.toString())
            false
        } else {
            true
        }
    } catch (e: Exception) {
        println("Error executing Docker command: ${e.message}")
        false
    }
}

// Docker Compose 명령어 실행
fun dockerComposeWithFile(file: String, vararg args: String): Boolean {
    return executeDockerCommand("compose", "-f", file, *args)
}

// ====================
// Infrastructure Tasks
// ====================

tasks.register("runInfraOnly") {
    group = "docker-infra"
    description = "Start only infrastructure (MySQL, Redis, Kafka)"
    doLast {
        println("🚀 Starting infrastructure services...")
        executeDockerCommand("network", "create", "asyncsite-network")

        if (dockerComposeWithFile("docker-compose-infra.yml", "up", "-d")) {
            println("✅ Infrastructure services started")
            println("📍 MySQL: localhost:3306")
            println("📍 Redis: localhost:6379")
            println("📍 Kafka: localhost:9092")
        }
    }
}

tasks.register("dockerDownInfra") {
    group = "docker-infra"
    description = "Stop infrastructure services"
    doLast {
        dockerComposeWithFile("docker-compose-infra.yml", "down")
        println("✅ Infrastructure stopped")
    }
}

tasks.register("dockerLogsInfra") {
    group = "docker-infra"
    description = "View infrastructure logs"
    doLast {
        dockerComposeWithFile("docker-compose-infra.yml", "logs", "-f")
    }
}

// ====================
// IAP Gateway Service Tasks
// ====================

tasks.register("dockerRebuildAndRunIAPOnly") {
    group = "docker-iap"
    description = "Rebuild and run IAP Gateway service with tests"
    doLast {
        println("🔨 Building IAP Gateway Service with tests...")

        // 1. Clean build with tests
        val buildResult = project.exec {
            commandLine("./gradlew", "clean", "build")
            isIgnoreExitValue = true
        }

        if (buildResult.exitValue != 0) {
            println("❌ Build failed. Fix the tests first!")
            return@doLast
        }

        println("✅ Build successful")

        // 2. Stop and rebuild
        dockerComposeWithFile("docker-compose-iap-gateway.yml", "down")
        executeDockerCommand("network", "create", "asyncsite-network")

        if (dockerComposeWithFile("docker-compose-iap-gateway.yml", "up", "-d", "--build")) {
            println("✅ IAP Gateway Service started")
            println("📍 Service URL: http://localhost:8089")
            println("📝 View logs: ./gradlew dockerLogsIAPOnly")
        }
    }
}

tasks.register("dockerQuickRebuildIAPOnly") {
    group = "docker-iap"
    description = "Quick rebuild (skip tests)"
    doLast {
        println("⚡ Quick rebuild...")
        project.exec {
            commandLine("./gradlew", "clean", "build", "-x", "test")
        }

        dockerComposeWithFile("docker-compose-iap-gateway.yml", "down")
        executeDockerCommand("network", "create", "asyncsite-network")

        if (dockerComposeWithFile("docker-compose-iap-gateway.yml", "up", "-d", "--build")) {
            println("✅ Quick rebuild completed")
        }
    }
}

tasks.register("dockerLogsIAPOnly") {
    group = "docker-iap"
    description = "View IAP Gateway service logs"
    doLast {
        dockerComposeWithFile("docker-compose-iap-gateway.yml", "logs", "-f", "iap-gateway-service")
    }
}

tasks.register("dockerDownIAPOnly") {
    group = "docker-iap"
    description = "Stop IAP Gateway service"
    doLast {
        dockerComposeWithFile("docker-compose-iap-gateway.yml", "down")
        println("✅ IAP Gateway Service stopped")
    }
}

// ====================
// Full Stack Tasks
// ====================

tasks.register("dockerRebuildAll") {
    group = "docker-all"
    description = "Rebuild and start all services"
    doLast {
        println("🚀 Starting all services...")
        executeDockerCommand("network", "create", "asyncsite-network")
        dockerComposeWithFile("docker-compose-infra.yml", "up", "-d")

        println("⏳ Waiting for infrastructure...")
        Thread.sleep(10000)

        project.tasks.getByName("dockerRebuildAndRunIAPOnly").actions.forEach {
            it.execute(project.tasks.getByName("dockerRebuildAndRunIAPOnly"))
        }
    }
}

tasks.register("dockerDownAll") {
    group = "docker-all"
    description = "Stop all services"
    doLast {
        dockerComposeWithFile("docker-compose-iap-gateway.yml", "down")
        dockerComposeWithFile("docker-compose-infra.yml", "down")
        println("✅ All services stopped")
    }
}

// ====================
// Utility Tasks
// ====================

tasks.register("dockerStatus") {
    group = "docker-utils"
    description = "Show Docker container status"
    doLast {
        println("\n📊 Docker Container Status:")
        executeDockerCommand("ps", "-a", "--filter", "name=asyncsite")
    }
}

tasks.register("dockerNetworkCreate") {
    group = "docker-utils"
    description = "Create asyncsite-network"
    doLast {
        if (executeDockerCommand("network", "create", "asyncsite-network")) {
            println("✅ Network 'asyncsite-network' created")
        } else {
            println("⚠️  Network already exists")
        }
    }
}
