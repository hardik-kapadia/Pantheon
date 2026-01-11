package com.pantheon.backend.worker;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.model.PlatformType;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Component
class LocalSteamLibraryScanner implements LocalGameLibraryScanner {

    @Override
    public PlatformType getSupportedType() {
        return PlatformType.API;
    }

    @Override
    public String getPlatformName() {
        return "Steam";
    }

    @Override
    public List<ScannedLocalGameDTO> scan(Path libraryPath) throws ScanFailureException {
        System.out.println("🔎 Steam Scanner: Analyzing VDF files in " + libraryPath);

        // MOCK DATA: Simulating a found game.
        return List.of(
                ScannedLocalGameDTO.builder()
                        .title("Counter-Strike 2")
                        .platformGameId("730")
                        .platformName("Steam")
                        .platformType(PlatformType.API)
                        .installPath(libraryPath.resolve("Counter-Strike Global Offensive").toString())
                        .isInstalled(true)
                        .lastPlayed(LocalDateTime.now().minusHours(2))
                        .playtimeMinutes(120)
                        .build()
        );
    }

}
