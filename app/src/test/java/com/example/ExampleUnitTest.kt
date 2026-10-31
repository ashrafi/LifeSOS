package com.example

import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MetaCyanSecondary
import com.example.ui.theme.MetaHorizonViolet
import com.example.ui.theme.MetaPrimaryBlue
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SerenityCoralPrimary
import com.example.ui.theme.SerenityMintSecondary
import com.example.ui.theme.ThemeState
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testThemeModeSwitching() {
    ThemeState.currentTheme = AppThemeMode.LUMINOUS_SERENITY
    assertEquals(SerenityCoralPrimary, CoralPrimary)
    assertEquals(SerenityMintSecondary, MintSecondary)

    ThemeState.currentTheme = AppThemeMode.META_HORIZON
    assertEquals(MetaPrimaryBlue, CoralPrimary)
    assertEquals(MetaCyanSecondary, MintSecondary)
    assertEquals(MetaHorizonViolet, SageAccent)

    ThemeState.currentTheme = AppThemeMode.LUMINOUS_SERENITY
    assertEquals(SerenityCoralPrimary, CoralPrimary)
  }

  @Test
  fun testByokSecurityConfigAndRevocation() {
    val repo = com.example.data.SerenityRepository(null)
    
    // Initially without context, key should not be configured
    assertEquals(false, repo.metaAiConfig.value.hasConfiguredKey)
    assertEquals(true, repo.metaAiConfig.value.isKeyEncryptedInKeystore)

    // Updating token sets hasConfiguredKey = true
    repo.updateMetaAiConfig(
      newToken = "gsk_test_mock_secret_key_12345",
      endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
      modelName = "llama-3.1-70b-versatile",
      isLocalPrivacyMode = false
    )
    assertEquals(true, repo.metaAiConfig.value.hasConfiguredKey)
    assertEquals(true, repo.metaAiConfig.value.isConnected)
    assertEquals("gsk_test_mock_secret_key_12345", repo.metaAiConfig.value.token)

    // User revokes / removes the BYOK key
    repo.removeMetaAiToken()
    assertEquals(false, repo.metaAiConfig.value.hasConfiguredKey)
    assertEquals(false, repo.metaAiConfig.value.isConnected)
    assertEquals("", repo.metaAiConfig.value.token)
    assertEquals(true, repo.metaAiConfig.value.isLocalLlamaPrivacyMode)
  }
}
