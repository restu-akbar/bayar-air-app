GRADLEW := ./gradlew
PKG ?= org.com.bayarair
.PHONY: build clean test run

tasks:
	$(GRADLEW) tasks 

build:
	$(GRADLEW) build

install:
	$(GRADLEW) installDebug 

apk:
	$(GRADLEW) :composeApp:buildAllArtifacts :composeApp:renameArtifacts 

clean:
	$(GRADLEW) clean

test:
	$(GRADLEW) test

run:
	$(GRADLEW) bootRun

logdroid:
	adb logcat -v color -s $(PKG)
