#!/bin/sh

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Create and write the pre-push hook
cat > .git/hooks/pre-push << 'EOF'
#!/bin/sh

echo "Running pre-push hook: gradle clean build"

# Run the build
./gradlew clean build

# If the build failed, exit with error code
if [ $? -ne 0 ]; then
    echo "Pre-push hook failed: gradle clean build failed"
    exit 1
fi

exit 0
EOF

# Make the hook executable
chmod +x .git/hooks/pre-push

echo "Git hooks installed successfully" 