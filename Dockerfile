# Multi-stage build for Aegis Android App
FROM eclipse-temurin:17 AS builder

WORKDIR /app

# Install Android SDK dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Download and install Android SDK
ENV ANDROID_SDK_ROOT=/android-sdk
RUN mkdir -p $ANDROID_SDK_ROOT && \
    cd $ANDROID_SDK_ROOT && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip -q commandlinetools-linux-9477386_latest.zip && \
    rm commandlinetools-linux-9477386_latest.zip && \
    mv cmdline-tools latest && \
    mkdir -p cmdline-tools && \
    mv latest cmdline-tools/

# Set environment variables
ENV PATH=$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH
ENV JAVA_HOME=/opt/java/openjdk

# Accept Android SDK licenses
RUN yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT --licenses

# Install required SDK components
RUN sdkmanager --sdk_root=$ANDROID_SDK_ROOT \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "platform-tools"

# Copy project files
COPY . /app

# Build the APK with Gradle
RUN chmod +x gradlew && \
    ./gradlew clean assembleDebug

# Final stage - extract APK
FROM alpine:latest

RUN apk add --no-cache openjdk17

WORKDIR /app

COPY --from=builder /app/app/build/outputs/apk/debug/*.apk /app/

RUN ls -la /app/*.apk

# Optional: Create a volume for output
VOLUME ["/app/output"]

# Copy built APK to output on container run
CMD cp /app/*.apk /app/output/ || true

ENTRYPOINT ["sh", "-c", "echo 'Aegis APK build complete. Check /app/output/' && sleep infinity"]
