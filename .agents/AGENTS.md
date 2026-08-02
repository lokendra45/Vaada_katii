# Project-Scoped Agent Rules

## Compose Multiplatform String Extraction
When building new UI screens, composables, or modifying existing features in this project, **always extract hardcoded UI strings** (like titles, button texts, error messages, and placeholders) into the project's central Compose Resources file located at `shared/src/commonMain/composeResources/values/strings.xml`. 

After extracting the strings to the XML file, use the generated type-safe string resource accessors in the Compose code:
```kotlin
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

// Example Usage:
Text(stringResource(Res.string.your_string_key))
```
Do not leave raw hardcoded strings in the UI code unless they are purely functional symbols (like emojis or debug tags).

## Design Aesthetics (Google Pay / Material 3)
When creating or modifying UI components, always prioritize a clean, polished, and highly elegant look inspired by Google Pay and other modern Google products.
- **Typography:** Use distinct typographic hierarchies (e.g., large, clean fonts for amounts/headers using `displayLarge` or `headlineMedium`).
- **Whitespace:** Emphasize breathing room. Avoid cramped interfaces by liberally using `Spacing.ItemGap`, `Spacing.SectionGap`, etc.
- **Shapes & Radiuses:** Embrace modern pill shapes and heavily rounded corners (`MaterialTheme.shapes.medium` and `large`) instead of sharp boxes.
- **Color Palette:** Do not overuse brand colors as backgrounds. Utilize high-contrast, clean palettes where the surface is clean/neutral (e.g., `surfaceContainerLowest`) and color is used specifically for emphasis, active states, and crisp borders.
- **Simplicity:** Ensure input fields and actionable areas feel intuitive, polished, and uncluttered (e.g., borderless text fields for major numeric inputs).
