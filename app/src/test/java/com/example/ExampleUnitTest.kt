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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
  fun testMuseSparkAsStarModel() {
    val repo = com.example.data.SerenityRepository(null)

    // Meta Muse Spark 1.3 is the default flagship star
    assertEquals(com.example.model.MetaModelEngine.MUSE_SPARK_1_3, repo.metaAiConfig.value.selectedEngine)
    assertEquals("muse-spark-1.3", repo.metaAiConfig.value.modelName)
    assertEquals("https://api.meta.ai/v1/chat/completions", repo.metaAiConfig.value.endpointUrl)
    assertEquals(false, repo.metaAiConfig.value.isLocalLlamaPrivacyMode)
    assertEquals(true, repo.metaAiConfig.value.isKeyEncryptedInKeystore)

    // Switch to On-Device Privacy Mode
    repo.selectModelEngine(com.example.model.MetaModelEngine.ON_DEVICE_LLAMA)
    assertEquals(com.example.model.MetaModelEngine.ON_DEVICE_LLAMA, repo.metaAiConfig.value.selectedEngine)
    assertEquals(true, repo.metaAiConfig.value.isLocalLlamaPrivacyMode)

    // Return to Flagship Meta Muse Spark 1.3
    repo.selectModelEngine(com.example.model.MetaModelEngine.MUSE_SPARK_1_3)
    assertEquals(com.example.model.MetaModelEngine.MUSE_SPARK_1_3, repo.metaAiConfig.value.selectedEngine)
    assertEquals("muse-spark-1.3", repo.metaAiConfig.value.modelName)
    assertEquals(false, repo.metaAiConfig.value.isLocalLlamaPrivacyMode)
  }

  @Test
  fun testByokSecurityConfigAndRevocation() {
    val repo = com.example.data.SerenityRepository(null)
    
    // Initially without context, key should not be configured
    assertEquals(false, repo.metaAiConfig.value.hasConfiguredKey)
    assertEquals(true, repo.metaAiConfig.value.isKeyEncryptedInKeystore)
    assertEquals("muse-spark-1.3", repo.metaAiConfig.value.modelName)

    // Updating token sets hasConfiguredKey = true
    repo.updateMetaAiConfig(
      newToken = "meta_spark_test_mock_secret_key_12345",
      endpointUrl = "https://api.meta.ai/v1/chat/completions",
      modelName = "muse-spark-1.3",
      isLocalPrivacyMode = false
    )
    assertEquals(true, repo.metaAiConfig.value.hasConfiguredKey)
    assertEquals(true, repo.metaAiConfig.value.isConnected)
    assertEquals("meta_spark_test_mock_secret_key_12345", repo.metaAiConfig.value.token)
    assertEquals(com.example.model.MetaModelEngine.MUSE_SPARK_1_3, repo.metaAiConfig.value.selectedEngine)

    // User revokes / removes the BYOK key
    repo.removeMetaAiToken()
    assertEquals(false, repo.metaAiConfig.value.hasConfiguredKey)
    assertEquals(false, repo.metaAiConfig.value.isConnected)
    assertEquals("", repo.metaAiConfig.value.token)
  }

  @Test
  fun testDefaultThemeIsMetaHorizon() {
    // Fresh installs (no saved preference) must open on the Meta Horizon brand theme
    val repo = com.example.data.SerenityRepository(null)
    assertEquals(AppThemeMode.META_HORIZON, repo.appThemeMode.value)
  }

  @Test
  fun testTtsVoiceDefaultsAndCoercion() {
    val repo = com.example.data.SerenityRepository(null)
    assertEquals(1.04f, repo.ttsPitch.value, 0.001f)
    assertEquals(0.96f, repo.ttsSpeechRate.value, 0.001f)

    // Out-of-range values are coerced into the 0.5–2.0 slider range
    repo.setTtsPitch(5.0f)
    repo.setTtsSpeechRate(-1.0f)
    assertEquals(2.0f, repo.ttsPitch.value, 0.001f)
    assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f)
  }

  @Test
  fun testRecoveryLabelsSayLoggedNotVerified() {
    // Demo recovery is self-reported logging, not sensor verification.
    // Sensor-confirmed states are the roadmap (Suggested → Logged → Sensor-Verified).
    val recovered = com.example.model.DemoRecoveryStage.RECOVERED_HAPPY
    assertTrue(recovered.label.contains("Logged"))
    assertFalse(recovered.label.contains("Verified"))
    val committed = com.example.model.DemoRecoveryStage.HOPEFUL_COMMITTED
    assertTrue(committed.label.contains("Logged"))
    assertFalse(committed.label.contains("Verified"))
  }

  @Test
  fun testDefaultSafetyStatusNeverClaimsClassifierPass() {
    // No separate classifier runs anywhere, so the default must disclose that —
    // never a model-gradeable PASSED verdict.
    val advice = com.example.model.MuseLifeAdvice(
      id = "t", timestamp = "t", headline = "h",
      overallDiagnosis = "d", pillars = emptyList(), tamagotchiPrescription = "p"
    )
    assertTrue(advice.llamaGuardStatus.contains("No Separate Classifier"))
    assertFalse(advice.llamaGuardStatus.contains("PASSED"))
  }
}
