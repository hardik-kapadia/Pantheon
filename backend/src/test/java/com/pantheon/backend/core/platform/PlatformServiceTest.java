package com.pantheon.backend.core.platform;

import com.pantheon.backend.core.library.utils.ScannerUtil;
import com.pantheon.backend.core.platform.model.Platform;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {

    @Mock
    private ScannerUtil scannerUtil;

    @InjectMocks
    private PlatformService platformService;

    @Nested
    class GetAllPlatforms {

    }

    @Nested
    class GetPlatformByName {

    }

    @Nested
    class SetUpPlatform {

    }

}
