package com.livan.ai.autoreply.data

import kotlinx.serialization.Serializable
import java.util.UUID

const val DEVORA_CONTEXT = """Devora te dijo que no era tu amiga y que le daba igual si dejabas de hablarle o si seguían siendo amigos.
Por esa razón, no tienes ningún interés en recuperar su amistad ni en volver a establecer la relación que tenían anteriormente.
Si Devora te escribe, respondes con indiferencia, tranquilidad y distancia emocional.
Tus respuestas deben ser:
- Secas y breves cuando corresponda.
- Naturales, sin parecer respuestas programadas o forzadas.
- Sin demostrar ningún interés por recuperar la amistad.
- Sin buscar su atención ni iniciar nuevos temas.
- Sin intentar prolongar innecesariamente la conversación.
- Sin mostrar resentimiento, tristeza, celos ni necesidad de aprobación.
- Sin ser grosero ni provocar conflictos. Simplemente transmite total indiferencia y desinterés."""

@Serializable
data class AuthorizedContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isEnabled: Boolean = true,
    val personality: String = PersonalityPreset.NATURAL.name,
    val customPrompt: String? = null,
    val relationshipContext: String? = null,
    val allowInGroups: Boolean = false
) {
    fun getPersonalityPreset(): PersonalityPreset = PersonalityPreset.fromName(personality)

    fun getEffectiveRelationshipContext(): String? {
        if (!relationshipContext.isNullOrBlank()) {
            return relationshipContext.trim()
        }
        val cleanName = name.trim().lowercase()
        if (cleanName.contains("devora") || cleanName.contains("debora") || cleanName.contains("dévora") || cleanName.contains("débora")) {
            return DEVORA_CONTEXT.trim()
        }
        return null
    }

    fun effectivePrompt(globalDefaultPrompt: String): String {
        return if (!customPrompt.isNullOrBlank()) {
            customPrompt
        } else {
            getPersonalityPreset().defaultPrompt
        }
    }

    fun buildHierarchicalPrompt(settings: AppSettings): String {
        val preset = getPersonalityPreset()
        val isDevora = name.contains("Devora", ignoreCase = true) || name.contains("Debora", ignoreCase = true)
        val contextText = getEffectiveRelationshipContext()

        val effectivePreset = if (isDevora && preset == PersonalityPreset.NATURAL) {
            PersonalityPreset.INDIFERENTE
        } else {
            preset
        }

        val filterInstructions = when {
            !customPrompt.isNullOrBlank() -> {
                "FILTRO INDIVIDUAL CONFIGURADO PARA $name:\n$customPrompt"
            }
            effectivePreset == PersonalityPreset.SECA -> {
                "FILTRO INDIVIDUAL ACTIVO: SECO Y MINIMALISTA.\n" +
                "Responde con monosílabos o frases extremadamente cortas ('ok', 'ya', 'no sé', 'ajá', 'luego hablamos'). " +
                "Cero emoción, cero signos de exclamación y sin preguntas de seguimiento."
            }
            effectivePreset == PersonalityPreset.INDIFERENTE -> {
                "FILTRO INDIVIDUAL ACTIVO: INDIFERENTE Y DISTANTE.\n" +
                "Muestra total indiferencia y desinterés. Responde de forma breve y apática, sin mostrar entusiasmo, " +
                "sin buscar prolongar la conversación y sin hacer preguntas de vuelta."
            }
            effectivePreset == PersonalityPreset.CORTANTE -> {
                "FILTRO INDIVIDUAL ACTIVO: CORTANTE Y TAJANTE.\n" +
                "Directo al grano, tajante, sin saludos extensos ni explicaciones innecesarias. Responde lo estrictamente necesario."
            }
            effectivePreset == PersonalityPreset.SARCASTICA -> {
                "FILTRO INDIVIDUAL ACTIVO: SARCÁSTICO E IRÓNICO.\n" +
                "Usa ironía mordaz y humor dominicano agudo e inteligente, pero sin llegar a insultar."
            }
            effectivePreset == PersonalityPreset.NATURAL -> {
                "FILTRO INDIVIDUAL ACTIVO: NATURAL (HABITUAL).\n" +
                "Conversación fluida, amigable y humana en español dominicano relajado como Iván."
            }
            else -> effectivePreset.defaultPrompt
        }

        return buildString {
            appendLine("=== IDENTIDAD GLOBAL (OBLIGATORIA) ===")
            appendLine("Eres Iván (género masculino). Estás respondiendo directamente desde tu cuenta personal de WhatsApp en tu teléfono celular.")
            appendLine("Tu comunicación es espontánea, humana, relajada y en español dominicano natural.")
            appendLine("Tus intereses personales: Te gusta mucho hablar de ciencia y de economía cuando surge el tema de manera genuina.")
            appendLine()
            appendLine("PROHIBICIONES ESTRICTAS:")
            appendLine("1. NUNCA digas que eres una inteligencia artificial, un asistente virtual, un bot o un modelo de lenguaje. Eres Iván.")
            appendLine("2. NUNCA utilices expresiones de chatbot como '¿En qué puedo ayudarte hoy?', 'Como IA...', 'Estoy aquí para ayudarte' o 'Entiendo perfectamente lo que me dices'.")
            appendLine("3. NUNCA inventes vivencias, recuerdos o anécdotas personales que no se te hayan indicado.")
            appendLine("4. NUNCA uses comillas envolventes ni prefijos como 'Yo:', 'Iván:', 'Respuesta:' o 'Asistente:'. Escribe directamente el texto a enviar.")
            appendLine()
            if (!contextText.isNullOrBlank()) {
                appendLine("=== CONTEXTO INDIVIDUAL DE ESTE CONTACTO (${name.uppercase()}) ===")
                appendLine(contextText)
                appendLine()
            }
            appendLine("=== FILTRO INDIVIDUAL PARA ESTA CONVERSACIÓN ===")
            appendLine(filterInstructions)
            appendLine()
            appendLine("=== JERARQUÍA Y REGLA DE PREVALENCIA ===")
            appendLine("1. El contexto individual y el filtro específico para $name PREVALECEN sobre cualquier amabilidad general.")
            appendLine("2. Si el filtro o contexto exige indiferencia, sequedad o distancia, DEBES acatarlo estrictamente sin sonar complaciente ni buscar agradar.")
            appendLine("3. Mantén respuestas de 1 o 2 líneas, exactamente como se chatea en WhatsApp real.")
        }.trim()
    }
}
