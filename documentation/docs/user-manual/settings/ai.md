# AI Configuration

## Configuration

The AI configuration page allows you to set up Artivact's artificial intelligence integration for features like
automatic translation and text-to-speech (TTS) audio generation.

The page is available from the system settings menu and can only be accessed by administrators.

## Settings

The following configuration options are available:

| Setting              | Description                                                                                                                                                                                                    |
|:---------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Enable AI            | Enables or disables all AI features in the application.                                                                                                                                                        |
| API Key              | The API key for authenticating with the AI service (e.g. OpenAI).                                                                                                                                              |
| Translation Prompt   | A prompt template used for automatic translations. The placeholder ``{locale}`` will be replaced by the target locale. See [I18N](../content-management/internationalization) for more details on translations. |
| TTS Voice            | The voice identifier used for text-to-speech audio generation (e.g. ``alloy``).                                                                                                                                |

::: warning API Key
The API key is required for all AI features to work. Without a valid API key, automatic translation and audio
generation will not be available.
:::

## Automatic Translation

When AI is enabled and configured, Artivact can automatically translate item texts and page content into
the configured locales using the AI service.

The translation is based on the **Translation Prompt**. You can customize the prompt
template to influence how the AI performs translations.

See [I18N](../content-management/internationalization) for details on managing multilingual content.

## Audio Content Generation (TTS)

When AI is enabled and configured, Artivact can generate audio content from text using text-to-speech.

The audio generation is based on the configured **TTS Voice**. The generated
audio files can be used e.g. in content exports.

The available voices depend on the configured AI provider.
