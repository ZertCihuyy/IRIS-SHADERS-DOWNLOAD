# IRIS SHEDERS DOWNLOAD

<p align="center">
  <img src="src/main/resources/assets/iris_shaders_download/icon.png" alt="Mod Icon" width="128">
</p>

Welcome to **IRIS SHEDERS DOWNLOAD**! This is a Fabric Minecraft mod for version 1.21.1 designed to completely automate your shader installation experience. 

## Features
- **Auto-Download**: If you don't have a shader pack installed, the mod will automatically download a popular shader (like Complementary Reimagined) from Modrinth directly into your `.minecraft/shaderpacks` folder. No manual drag-and-drop needed!
- **Auto-Apply**: The mod will automatically modify your `iris.properties` configuration to ensure that Iris Shaders is enabled and the downloaded shader pack is actively selected as soon as you open the game.
- **GitHub Actions Ready**: This repository contains an automated GitHub Actions workflow (`.github/workflows/release.yml`). Pushing a tag like `v1.0.0` will automatically compile a ready-to-use `.jar` release.

## Full Code Explanation
The core logic resides in the `IrisShadersDownloadClient.java` file, which runs as a `ClientModInitializer`. 
1. **Directory Check**: It first checks if the `shaderpacks` directory exists inside the Minecraft game directory, and creates it if missing.
2. **Download Execution**: It performs an HTTP GET request to the Modrinth API to find the latest version of the target shader, parses the response, and downloads the `.zip` file.
3. **Configuration Injection**: It reads `config/iris.properties`, updates the `enableShaders` and `shaderPack` keys, and saves the file. This forcefully activates the shader without user intervention.

## License
This project is licensed under the **GPLv3** (GNU General Public License v3.0). You are free to modify, distribute, and use this code as long as your changes are also open source under the same license. See the `fabric.mod.json` file where the license is declared.

## How to use
1. Drop the mod `.jar` file into your `.minecraft/mods` folder.
2. Launch Minecraft (make sure Fabric API and Iris are installed).
3. Jump into a world, and enjoy your beautiful new shaders instantly!

---
*Note: This code is suboptimal because it was created using AI, etc.*
