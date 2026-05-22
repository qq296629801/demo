package com.sie.iidp.example.sitemanager;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.ConfigUtils;
import com.sie.snest.test.*;
import com.sie.snest.test.dto.ExpectedError;
import com.sie.snest.test.mock.RecordSetMock;
import org.mockito.MockedStatic;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@IIDPTest(value = true, engine = true) // value=true加载Spring上下文，engine=true加载引擎上下文
class SiteManagerTest {

    @DDTest
    void tryConnection(@DDArgs String cloudAddress, @DDArgs String protocol, @DDArgs String applicationId,@DDArgs boolean clearLicense, @DDExpected Boolean result, @DDExpected ExpectedError error) {
        SiteManager siteManager = new SiteManager();
        RecordSet mockRecordSet = RecordSetMock.spy( "ops_cloud_registration_manager");
        doReturn(true).when(mockRecordSet).call(eq("tryConnectionSign"), eq(cloudAddress),
                argThat(arg -> null == arg || "".equals(protocol) || arg.equals(protocol)), anyString(), eq(applicationId));
        try {
            if(clearLicense) {
                try (MockedStatic<ConfigUtils> mockedStatic = mockStatic(ConfigUtils.class)) {
                    mockedStatic.when(() -> ConfigUtils.get("iidp.hc.license")).thenReturn(null);
                }
            }
            Boolean actual = siteManager.tryConnection(cloudAddress, protocol, applicationId);
            assertEquals(result, actual);
        } catch (Exception e) {
            assertInstanceOf(ValidationException.class, e);
            assertTrue(e.getMessage().contains(error.getMessage()));
        }
    }
}