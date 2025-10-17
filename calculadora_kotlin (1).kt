import kotlin.math.*
import java.util.*

// ============================================
// EXCEPCIONES PERSONALIZADAS
// ============================================

/**
 * Excepción base para errores de cálculo
 */
open class CalculadoraException(message: String) : Exception(message)

/**
 * Excepción para división por cero
 */
class DivisionPorCeroException : CalculadoraException("Error: División por cero no permitida")

/**
 * Excepción para operaciones matemáticas inválidas
 */
class OperacionInvalidaException(mensaje: String) : CalculadoraException(mensaje)

/**
 * Excepción para expresiones mal formadas
 */
class ExpresionInvalidaException(mensaje: String) : CalculadoraException(mensaje)

// ============================================
// CLASE BASE: CALCULADORA
// ============================================

/**
 * Clase base que implementa operaciones aritméticas básicas
 * Principio de POO: Encapsulamiento
 */
open class Calculadora {
    
    // Memoria de la calculadora (Encapsulamiento)
    protected var memoria: Double = 0.0
    
    // Historial de operaciones
    protected val historial: MutableList<String> = mutableListOf()
    
    /**
     * Suma dos números
     * Principio: Polimorfismo (sobrecarga)
     */
    open fun sumar(a: Double, b: Double): Double {
        val resultado = a + b
        registrarOperacion("$a + $b = $resultado")
        return resultado
    }
    
    open fun sumar(a: Int, b: Int): Int {
        val resultado = a + b
        registrarOperacion("$a + $b = $resultado")
        return resultado
    }
    
    /**
     * Resta dos números
     * Principio: Polimorfismo (sobrecarga)
     */
    open fun restar(a: Double, b: Double): Double {
        val resultado = a - b
        registrarOperacion("$a - $b = $resultado")
        return resultado
    }
    
    open fun restar(a: Int, b: Int): Int {
        val resultado = a - b
        registrarOperacion("$a - $b = $resultado")
        return resultado
    }
    
    /**
     * Multiplica dos números
     * Principio: Polimorfismo (sobrecarga)
     */
    open fun multiplicar(a: Double, b: Double): Double {
        val resultado = a * b
        registrarOperacion("$a × $b = $resultado")
        return resultado
    }
    
    open fun multiplicar(a: Int, b: Int): Int {
        val resultado = a * b
        registrarOperacion("$a × $b = $resultado")
        return resultado
    }
    
    /**
     * Divide dos números
     * Manejo de excepciones para división por cero
     * Principio: Polimorfismo (sobrecarga)
     */
    @Throws(DivisionPorCeroException::class)
    open fun dividir(a: Double, b: Double): Double {
        if (b == 0.0) {
            throw DivisionPorCeroException()
        }
        val resultado = a / b
        registrarOperacion("$a ÷ $b = $resultado")
        return resultado
    }
    
    @Throws(DivisionPorCeroException::class)
    open fun dividir(a: Int, b: Int): Double {
        if (b == 0) {
            throw DivisionPorCeroException()
        }
        val resultado = a.toDouble() / b.toDouble()
        registrarOperacion("$a ÷ $b = $resultado")
        return resultado
    }
    
    // ============================================
    // FUNCIONES DE MEMORIA
    // ============================================
    
    /**
     * Guarda un valor en memoria (M+)
     */
    fun memoriaAgregar(valor: Double) {
        memoria += valor
        registrarOperacion("M+ $valor → Memoria: $memoria")
    }
    
    /**
     * Resta un valor de memoria (M-)
     */
    fun memoriaRestar(valor: Double) {
        memoria -= valor
        registrarOperacion("M- $valor → Memoria: $memoria")
    }
    
    /**
     * Recupera el valor de memoria (MR)
     */
    fun memoriaRecuperar(): Double {
        registrarOperacion("MR → $memoria")
        return memoria
    }
    
    /**
     * Limpia la memoria (MC)
     */
    fun memoriaLimpiar() {
        memoria = 0.0
        registrarOperacion("MC → Memoria limpia")
    }
    
    /**
     * Guarda directamente en memoria (MS)
     */
    fun memoriaGuardar(valor: Double) {
        memoria = valor
        registrarOperacion("MS $valor → Memoria: $memoria")
    }
    
    // ============================================
    // UTILIDADES
    // ============================================
    
    /**
     * Registra una operación en el historial
     */
    protected fun registrarOperacion(operacion: String) {
        historial.add(operacion)
    }
    
    /**
     * Obtiene el historial de operaciones
     */
    fun obtenerHistorial(): List<String> {
        return historial.toList()
    }
    
    /**
     * Limpia el historial
     */
    fun limpiarHistorial() {
        historial.clear()
    }
    
    /**
     * Obtiene la última operación
     */
    fun ultimaOperacion(): String? {
        return historial.lastOrNull()
    }
}

// ============================================
// CLASE DERIVADA: CALCULADORA CIENTÍFICA
// ============================================

/**
 * Calculadora Científica que extiende la funcionalidad básica
 * Principio de POO: Herencia
 */
class CalculadoraCientifica : Calculadora() {
    
    companion object {
        const val PI = Math.PI
        const val E = Math.E
    }
    
    // ============================================
    // FUNCIONES TRIGONOMÉTRICAS
    // ============================================
    
    /**
     * Calcula el seno de un ángulo en radianes
     */
    fun seno(angulo: Double): Double {
        val resultado = sin(angulo)
        registrarOperacion("sin($angulo) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el coseno de un ángulo en radianes
     */
    fun coseno(angulo: Double): Double {
        val resultado = cos(angulo)
        registrarOperacion("cos($angulo) = $resultado")
        return resultado
    }
    
    /**
     * Calcula la tangente de un ángulo en radianes
     */
    @Throws(OperacionInvalidaException::class)
    fun tangente(angulo: Double): Double {
        // Verificar si el ángulo es múltiplo de π/2 (tangente indefinida)
        val moduloPI = angulo % PI
        if (abs(moduloPI - PI/2) < 1e-10 || abs(moduloPI + PI/2) < 1e-10) {
            throw OperacionInvalidaException("Tangente indefinida para π/2 + nπ")
        }
        val resultado = tan(angulo)
        registrarOperacion("tan($angulo) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el arcoseno (seno inverso)
     */
    @Throws(OperacionInvalidaException::class)
    fun arcoSeno(valor: Double): Double {
        if (valor < -1.0 || valor > 1.0) {
            throw OperacionInvalidaException("arcsin está definido solo para [-1, 1]")
        }
        val resultado = asin(valor)
        registrarOperacion("arcsin($valor) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el arcocoseno (coseno inverso)
     */
    @Throws(OperacionInvalidaException::class)
    fun arcoCoseno(valor: Double): Double {
        if (valor < -1.0 || valor > 1.0) {
            throw OperacionInvalidaException("arccos está definido solo para [-1, 1]")
        }
        val resultado = acos(valor)
        registrarOperacion("arccos($valor) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el arcotangente (tangente inversa)
     */
    fun arcoTangente(valor: Double): Double {
        val resultado = atan(valor)
        registrarOperacion("arctan($valor) = $resultado")
        return resultado
    }
    
    // ============================================
    // CONVERSIONES ANGULARES
    // ============================================
    
    /**
     * Convierte grados a radianes
     */
    fun gradosARadianes(grados: Double): Double {
        val radianes = Math.toRadians(grados)
        registrarOperacion("$grados° = $radianes rad")
        return radianes
    }
    
    /**
     * Convierte radianes a grados
     */
    fun radianesAGrados(radianes: Double): Double {
        val grados = Math.toDegrees(radianes)
        registrarOperacion("$radianes rad = $grados°")
        return grados
    }
    
    // ============================================
    // POTENCIAS Y RAÍCES
    // ============================================
    
    /**
     * Calcula la potencia de un número
     * Principio: Polimorfismo (sobrecarga)
     */
    fun potencia(base: Double, exponente: Double): Double {
        val resultado = base.pow(exponente)
        registrarOperacion("$base ^ $exponente = $resultado")
        return resultado
    }
    
    fun potencia(base: Int, exponente: Int): Double {
        return potencia(base.toDouble(), exponente.toDouble())
    }
    
    /**
     * Calcula la raíz cuadrada
     */
    @Throws(OperacionInvalidaException::class)
    fun raizCuadrada(numero: Double): Double {
        if (numero < 0) {
            throw OperacionInvalidaException("No se puede calcular raíz cuadrada de número negativo")
        }
        val resultado = sqrt(numero)
        registrarOperacion("√$numero = $resultado")
        return resultado
    }
    
    /**
     * Calcula la raíz n-ésima
     */
    @Throws(OperacionInvalidaException::class)
    fun raizN(numero: Double, n: Double): Double {
        if (numero < 0 && n % 2 == 0.0) {
            throw OperacionInvalidaException("No se puede calcular raíz par de número negativo")
        }
        val resultado = numero.pow(1.0 / n)
        registrarOperacion("$numero ^ (1/$n) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el cuadrado de un número
     */
    fun cuadrado(numero: Double): Double {
        return potencia(numero, 2.0)
    }
    
    /**
     * Calcula el cubo de un número
     */
    fun cubo(numero: Double): Double {
        return potencia(numero, 3.0)
    }
    
    // ============================================
    // LOGARITMOS Y EXPONENCIALES
    // ============================================
    
    /**
     * Calcula el logaritmo en base 10
     */
    @Throws(OperacionInvalidaException::class)
    fun logaritmoBase10(numero: Double): Double {
        if (numero <= 0) {
            throw OperacionInvalidaException("Logaritmo solo definido para números positivos")
        }
        val resultado = log10(numero)
        registrarOperacion("log₁₀($numero) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el logaritmo natural (base e)
     */
    @Throws(OperacionInvalidaException::class)
    fun logaritmoNatural(numero: Double): Double {
        if (numero <= 0) {
            throw OperacionInvalidaException("Logaritmo solo definido para números positivos")
        }
        val resultado = ln(numero)
        registrarOperacion("ln($numero) = $resultado")
        return resultado
    }
    
    /**
     * Calcula el logaritmo en cualquier base
     */
    @Throws(OperacionInvalidaException::class)
    fun logaritmo(numero: Double, base: Double): Double {
        if (numero <= 0 || base <= 0 || base == 1.0) {
            throw OperacionInvalidaException("Logaritmo inválido: número y base deben ser positivos, base ≠ 1")
        }
        val resultado = log(numero, base)
        registrarOperacion("log_$base($numero) = $resultado")
        return resultado
    }
    
    /**
     * Calcula e^x (exponencial natural)
     */
    fun exponencial(x: Double): Double {
        val resultado = exp(x)
        registrarOperacion("e^$x = $resultado")
        return resultado
    }
    
    /**
     * Calcula 10^x
     */
    fun exponencialBase10(x: Double): Double {
        val resultado = 10.0.pow(x)
        registrarOperacion("10^$x = $resultado")
        return resultado
    }
    
    // ============================================
    // OTRAS FUNCIONES MATEMÁTICAS
    // ============================================
    
    /**
     * Calcula el valor absoluto
     */
    fun valorAbsoluto(numero: Double): Double {
        val resultado = abs(numero)
        registrarOperacion("|$numero| = $resultado")
        return resultado
    }
    
    /**
     * Calcula el factorial de un número
     */
    @Throws(OperacionInvalidaException::class)
    fun factorial(n: Int): Long {
        if (n < 0) {
            throw OperacionInvalidaException("Factorial no definido para números negativos")
        }
        if (n > 20) {
            throw OperacionInvalidaException("Factorial muy grande (máximo 20)")
        }
        var resultado = 1L
        for (i in 2..n) {
            resultado *= i
        }
        registrarOperacion("$n! = $resultado")
        return resultado
    }
    
    /**
     * Calcula el módulo (resto de división)
     */
    @Throws(DivisionPorCeroException::class)
    fun modulo(a: Double, b: Double): Double {
        if (b == 0.0) {
            throw DivisionPorCeroException()
        }
        val resultado = a % b
        registrarOperacion("$a mod $b = $resultado")
        return resultado
    }
    
    /**
     * Redondea un número
     */
    fun redondear(numero: Double, decimales: Int = 0): Double {
        val factor = 10.0.pow(decimales)
        val resultado = round(numero * factor) / factor
        registrarOperacion("round($numero, $decimales) = $resultado")
        return resultado
    }
}

// ============================================
// EVALUADOR DE EXPRESIONES
// ============================================

/**
 * Evaluador de expresiones matemáticas completas
 * Soporta operadores, funciones y paréntesis
 */
class EvaluadorExpresiones(private val calculadora: CalculadoraCientifica) {
    
    /**
     * Evalúa una expresión completa
     * Ejemplo: "2 + 3 * sin(45) - log(10)"
     */
    @Throws(ExpresionInvalidaException::class)
    fun evaluar(expresion: String): Double {
        try {
            // Reemplazar constantes
            var expr = expresion.replace("pi", PI.toString())
                                .replace("PI", PI.toString())
                                .replace("e", E.toString())
                                .replace("E", E.toString())
            
            // Reemplazar funciones con sus valores calculados
            expr = procesarFunciones(expr)
            
            // Evaluar la expresión numérica resultante
            return evaluarExpresionNumerica(expr)
            
        } catch (e: Exception) {
            throw ExpresionInvalidaException("Error al evaluar expresión: ${e.message}")
        }
    }
    
    /**
     * Procesa funciones matemáticas en la expresión
     */
    private fun procesarFunciones(expresion: String): String {
        var expr = expresion
        
        // Funciones trigonométricas
        expr = procesarFuncion(expr, "sin") { calculadora.seno(it) }
        expr = procesarFuncion(expr, "cos") { calculadora.coseno(it) }
        expr = procesarFuncion(expr, "tan") { calculadora.tangente(it) }
        expr = procesarFuncion(expr, "asin") { calculadora.arcoSeno(it) }
        expr = procesarFuncion(expr, "acos") { calculadora.arcoCoseno(it) }
        expr = procesarFuncion(expr, "atan") { calculadora.arcoTangente(it) }
        
        // Logaritmos
        expr = procesarFuncion(expr, "log10") { calculadora.logaritmoBase10(it) }
        expr = procesarFuncion(expr, "log") { calculadora.logaritmoBase10(it) }
        expr = procesarFuncion(expr, "ln") { calculadora.logaritmoNatural(it) }
        
        // Raíces y potencias
        expr = procesarFuncion(expr, "sqrt") { calculadora.raizCuadrada(it) }
        expr = procesarFuncion(expr, "exp") { calculadora.exponencial(it) }
        expr = procesarFuncion(expr, "abs") { calculadora.valorAbsoluto(it) }
        
        // Conversiones
        expr = procesarFuncion(expr, "rad") { calculadora.gradosARadianes(it) }
        expr = procesarFuncion(expr, "deg") { calculadora.radianesAGrados(it) }
        
        return expr
    }
    
    /**
     * Procesa una función específica en la expresión
     */
    private fun procesarFuncion(expresion: String, nombreFuncion: String, funcion: (Double) -> Double): String {
        var expr = expresion
        val regex = Regex("$nombreFuncion\\(([^)]+)\\)")
        
        while (true) {
            val match = regex.find(expr) ?: break
            val argumento = match.groupValues[1]
            val valorArgumento = evaluarExpresionNumerica(argumento)
            val resultado = funcion(valorArgumento)
            expr = expr.replaceRange(match.range, resultado.toString())
        }
        
        return expr
    }
    
    /**
     * Evalúa una expresión numérica simple (sin funciones)
     */
    private fun evaluarExpresionNumerica(expresion: String): Double {
        // Tokenizar
        val tokens = tokenizar(expresion)
        
        // Convertir a notación postfija (RPN)
        val rpn = infijoAPostfijo(tokens)
        
        // Evaluar RPN
        return evaluarRPN(rpn)
    }
    
    /**
     * Tokeniza una expresión
     */
    private fun tokenizar(expresion: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        var numeroActual = ""
        
        while (i < expresion.length) {
            val char = expresion[i]
            
            when {
                char.isWhitespace() -> {
                    if (numeroActual.isNotEmpty()) {
                        tokens.add(numeroActual)
                        numeroActual = ""
                    }
                }
                char.isDigit() || char == '.' -> {
                    numeroActual += char
                }
                char in "+-*/^()%" -> {
                    if (numeroActual.isNotEmpty()) {
                        tokens.add(numeroActual)
                        numeroActual = ""
                    }
                    tokens.add(char.toString())
                }
            }
            i++
        }
        
        if (numeroActual.isNotEmpty()) {
            tokens.add(numeroActual)
        }
        
        return tokens
    }
    
    /**
     * Convierte notación infija a postfija (algoritmo Shunting Yard)
     */
    private fun infijoAPostfijo(tokens: List<String>): List<String> {
        val salida = mutableListOf<String>()
        val operadores = Stack<String>()
        
        val precedencia = mapOf(
            "+" to 1, "-" to 1,
            "*" to 2, "/" to 2, "%" to 2,
            "^" to 3
        )
        
        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> salida.add(token)
                token == "(" -> operadores.push(token)
                token == ")" -> {
                    while (operadores.isNotEmpty() && operadores.peek() != "(") {
                        salida.add(operadores.pop())
                    }
                    if (operadores.isNotEmpty()) operadores.pop() // Eliminar "("
                }
                token in precedencia -> {
                    while (operadores.isNotEmpty() && 
                           operadores.peek() != "(" &&
                           precedencia.getOrDefault(operadores.peek(), 0) >= precedencia[token]!!) {
                        salida.add(operadores.pop())
                    }
                    operadores.push(token)
                }
            }
        }
        
        while (operadores.isNotEmpty()) {
            salida.add(operadores.pop())
        }
        
        return salida
    }
    
    /**
     * Evalúa una expresión en notación postfija (RPN)
     */
    private fun evaluarRPN(rpn: List<String>): Double {
        val pila = Stack<Double>()
        
        for (token in rpn) {
            when {
                token.toDoubleOrNull() != null -> pila.push(token.toDouble())
                token == "+" -> {
                    val b = pila.pop()
                    val a = pila.pop()
                    pila.push(calculadora.sumar(a, b))
                }
                token == "-" -> {
                    val b = pila.pop()
                    val a = pila.pop()
                    pila.push(calculadora.restar(a, b))
                }
                token == "*" -> {
                    val b = pila.pop()
                    val a = pila.pop()
                    pila.push(calculadora.multiplicar(a, b))
                }
                token == "/" -> {
                    val b = pila.pop()
                    val a = pila.pop()
                    pila.push(calculadora.dividir(a, b))
                }
                token == "^" -> {
                    val b = pila.pop()
                    val a = pila.pop()
                    pila.push(calculadora.potencia(a, b))
                }
                token == "%" -> {
                    val b = pila.pop()
                    val a = pila.pop()
                    pila.push(calculadora.modulo(a, b))
                }
            }
        }
        
        return pila.pop()
    }
}

// ============================================
// INTERFAZ DE USUARIO (CONSOLA)
// ============================================

/**
 * Interfaz de consola para la calculadora científica
 */
class InterfazCalculadora {
    private val calculadora = CalculadoraCientifica()
    private val evaluador = EvaluadorExpresiones(calculadora)
    private val scanner = Scanner(System.`in`)
    
    fun iniciar() {
        mostrarBienvenida()
        
        while (true) {
            mostrarMenu()
            print("\n🔢 Seleccione una opción: ")
            
            when (scanner.nextLine().trim()) {
                "1" -> operacionesBasicas()
                "2" -> funcionesTrigonometricas()
                "3" -> potenciasRaices()
                "4" -> logaritmosExponenciales()
                "5" -> evaluarExpresion()
                "6" -> gestionarMemoria()
                "7" -> mostrarHistorial()
                "8" -> ayuda()
                "0" -> {
                    println("\n👋 ¡Gracias por usar la Calculadora Científica!")
                    break
                }
                else -> println("❌ Opción inválida")
            }
        }
    }
    
    private fun mostrarBienvenida() {
        println("""
        ╔════════════════════════════════════════════════════════╗
        ║                                                        ║
        ║       🧮 CALCULADORA CIENTÍFICA EN KOTLIN 🧮          ║
        ║              Programación Orientada a Objetos         ║
        ║                                                        ║
        ╚════════════════════════════════════════════════════════╝
        """.trimIndent())
    }
    
    private fun mostrarMenu() {
        println("""
        
        ╔════════════════════════════════════════════════════════╗
        ║                     MENÚ PRINCIPAL                     ║
        ╠════════════════════════════════════════════════════════╣
        ║  1️⃣  Operaciones Básicas (+, -, ×, ÷)                 ║
        ║  2️⃣  Funciones Trigonométricas                        ║
        ║  3️⃣  Potencias y Raíces                               ║
        ║  4️⃣  Logaritmos y Exponenciales                       ║
        ║  5️⃣  Evaluar Expresión Completa                       ║
        ║  6️⃣  Gestión de Memoria (M+, M-, MR, MC)              ║
        ║  7️⃣  Ver Historial                                    ║
        ║  8️⃣  Ayuda                                            ║
        ║  0️⃣  Salir                                            ║
        ╚════════════════════════════════════════════════════════╝
        """.trimIndent())
    }
    
    private fun operacionesBasicas() {
        println("\n➕ OPERACIONES BÁSICAS")
        println("1. Suma  2. Resta  3. Multiplicación  4. División")
        print("Seleccione: ")
        
        val opcion = scanner.nextLine().trim()
        print("Primer número: ")
        val a = leerNumero()
        print("Segundo número: ")
        val b = leerNumero()
        
        try {
            val resultado = when (opcion) {
                "1" -> calculadora.sumar(a, b)
                "2" -> calculadora.restar(a, b)
                "3" -> calculadora.multiplicar(a, b)
                "4" -> calculadora.dividir(a, b)
                else -> {
                    println("Opción inválida")
                    return
                }
            }
            println("\n✅ Resultado: $resultado")
        } catch (e: CalculadoraException) {
            println("\n❌ ${e.message}")
        }
    }
    
    private fun funcionesTrigonometricas() {
        println("\n📐 FUNCIONES TRIGONOMÉTRICAS")
        println("1. Seno  2. Coseno  3. Tangente")
        println("4. Arcoseno  5. Arcocoseno  6. Arcotangente")
        println("7. Grados→Radianes  8. Radianes→Grados")
        print("Seleccione: ")
        
        val opcion = scanner.nextLine().trim()
        print("Ingrese el valor: ")
        val valor = leerNumero()
        
        try {
            val resultado = when (opcion) {
                "1" -> calculadora.seno(valor)
                "2" -> calculadora.coseno(valor)
                "3" -> calculadora.tangente(valor)
                "4" -> calculadora.arcoSeno(valor)
                "5" -> calculadora.arcoCoseno(valor)
                "6" -> calculadora.arcoTangente(valor)
                "7" -> calculadora.gradosARadianes(valor)
                "8" -> calculadora.radianesAGrados(valor)
                else -> {
                    println("Opción inválida")
                    return
                }
            }
            println("\n✅ Resultado: $resultado")
        } catch (e: CalculadoraException) {
            println("\n❌ ${e.message}")
        }
    }
    
    private fun potenciasRaices() {
        println("\n⚡ POTENCIAS Y RAÍCES")
        println("1. Potencia  2. Raíz cuadrada  3. Raíz n-ésima")
        println("4. Cuadrado  5. Cubo  6. Factorial")
        print("Seleccione: ")
        
        val opcion = scanner.nextLine().trim()
        
        try {
            val resultado = when (opcion) {
                "1" -> {
                    print("Base: ")
                    val base = leerNumero()
                    print("Exponente: ")
                    val exp = leerNumero()
                    calculadora.potencia(base, exp)
                }
                "2" -> {
                    print("Número: ")
                    calculadora.raizCuadrada(leerNumero())
                }
                "3" -> {
                    print("Número: ")
                    val num = leerNumero()
                    print("Índice de raíz: ")
                    val n = leerNumero()
                    calculadora.raizN(num, n)
                }
                "4" -> {
                    print("Número: ")
                    calculadora.cuadrado(leerNumero())
                }
                "5" -> {
                    print("Número: ")
                    calculadora.cubo(leerNumero())
                }
                "6" -> {
                    print("Número entero: ")
                    val n = scanner.nextLine().toIntOrNull() ?: 0
                    calculadora.factorial(n).toDouble()
                }
                else -> {
                    println("Opción inválida")
                    return
                }
            }
            println("\n✅ Resultado: $resultado")
        } catch (e: CalculadoraException) {
            println("\n❌ ${e.message}")
        }
    }
    
    private fun logaritmosExponenciales() {
        println("\n📊 LOGARITMOS Y EXPONENCIALES")
        println("1. Log base 10  2. Log natural (ln)  3. Log base n")
        println("4. e^x  5. 10^x  6. Valor absoluto")
        print("Seleccione: ")
        
        val opcion = scanner.nextLine().trim()
        
        try {
            val resultado = when (opcion) {
                "1" -> {
                    print("Número: ")
                    calculadora.logaritmoBase10(leerNumero())
                }
                "2" -> {
                    print("Número: ")
                    calculadora.logaritmoNatural(leerNumero())
                }
                "3" -> {
                    print("Número: ")
                    val num = leerNumero()
                    print("Base: ")
                    val base = leerNumero()
                    calculadora.logaritmo(num, base)
                }
                "4" -> {
                    print("Exponente (x): ")
                    calculadora.exponencial(leerNumero())
                }
                "5" -> {
                    print("Exponente (x): ")
                    calculadora.exponencialBase10(leerNumero())
                }
                "6" -> {
                    print("Número: ")
                    calculadora.valorAbsoluto(leerNumero())
                }
                else -> {
                    println("Opción inválida")
                    return
                }
            }
            println("\n✅ Resultado: $resultado")
        } catch (e: CalculadoraException) {
            println("\n❌ ${e.message}")
        }
    }
    
    private fun evaluarExpresion() {
        println("\n🧮 EVALUAR EXPRESIÓN COMPLETA")
        println("Ejemplos:")
        println("  • 2 + 3 * 4")
        println("  • (5 + 3) * 2")
        println("  • sin(45) + cos(30)")
        println("  • 2 * log10(100) + sqrt(16)")
        println("  • pi * 2^3")
        println("\nFunciones disponibles:")
        println("  Trigonométricas: sin, cos, tan, asin, acos, atan")
        println("  Logaritmos: log, log10, ln")
        println("  Otras: sqrt, exp, abs, rad (grados→rad), deg (rad→grados)")
        println("  Constantes: pi, e")
        
        print("\n📝 Ingrese la expresión: ")
        val expresion = scanner.nextLine().trim()
        
        try {
            val resultado = evaluador.evaluar(expresion)
            println("\n✅ Resultado: $resultado")
        } catch (e: Exception) {
            println("\n❌ Error: ${e.message}")
        }
    }
    
    private fun gestionarMemoria() {
        println("\n💾 GESTIÓN DE MEMORIA")
        println("Memoria actual: ${calculadora.memoriaRecuperar()}")
        println("\n1. M+ (Agregar)  2. M- (Restar)  3. MR (Recuperar)")
        println("4. MS (Guardar)  5. MC (Limpiar)")
        print("Seleccione: ")
        
        when (scanner.nextLine().trim()) {
            "1" -> {
                print("Valor a agregar: ")
                calculadora.memoriaAgregar(leerNumero())
                println("✅ Memoria actualizada: ${calculadora.memoriaRecuperar()}")
            }
            "2" -> {
                print("Valor a restar: ")
                calculadora.memoriaRestar(leerNumero())
                println("✅ Memoria actualizada: ${calculadora.memoriaRecuperar()}")
            }
            "3" -> {
                val valor = calculadora.memoriaRecuperar()
                println("✅ Valor en memoria: $valor")
            }
            "4" -> {
                print("Valor a guardar: ")
                calculadora.memoriaGuardar(leerNumero())
                println("✅ Memoria actualizada: ${calculadora.memoriaRecuperar()}")
            }
            "5" -> {
                calculadora.memoriaLimpiar()
                println("✅ Memoria limpiada")
            }
            else -> println("❌ Opción inválida")
        }
    }
    
    private fun mostrarHistorial() {
        println("\n📜 HISTORIAL DE OPERACIONES")
        val historial = calculadora.obtenerHistorial()
        
        if (historial.isEmpty()) {
            println("No hay operaciones en el historial")
        } else {
            historial.forEachIndexed { index, operacion ->
                println("${index + 1}. $operacion")
            }
            
            print("\n¿Limpiar historial? (s/n): ")
            if (scanner.nextLine().trim().lowercase() == "s") {
                calculadora.limpiarHistorial()
                println("✅ Historial limpiado")
            }
        }
    }
    
    private fun ayuda() {
        println("""
        
        ╔════════════════════════════════════════════════════════╗
        ║                        AYUDA                           ║
        ╠════════════════════════════════════════════════════════╣
        ║                                                        ║
        ║  📌 OPERACIONES BÁSICAS                                ║
        ║     • Suma, resta, multiplicación, división            ║
        ║     • Soporta enteros y decimales                      ║
        ║                                                        ║
        ║  📌 FUNCIONES TRIGONOMÉTRICAS                          ║
        ║     • Trabajan en RADIANES por defecto                 ║
        ║     • Use conversión grados↔radianes si es necesario   ║
        ║                                                        ║
        ║  📌 EXPRESIONES COMPLETAS                              ║
        ║     • Respeta precedencia de operadores                ║
        ║     • Soporta paréntesis anidados                      ║
        ║     • Constantes: pi (3.14159...), e (2.71828...)      ║
        ║                                                        ║
        ║  📌 MEMORIA                                            ║
        ║     • M+: Suma a memoria                               ║
        ║     • M-: Resta de memoria                             ║
        ║     • MR: Recupera valor                               ║
        ║     • MS: Guarda valor                                 ║
        ║     • MC: Limpia memoria                               ║
        ║                                                        ║
        ║  📌 MANEJO DE ERRORES                                  ║
        ║     • División por cero                                ║
        ║     • Logaritmo de números no positivos                ║
        ║     • Raíz cuadrada de números negativos               ║
        ║     • Tangente en π/2                                  ║
        ║     • Arcoseno/arcoseno fuera de [-1, 1]               ║
        ║                                                        ║
        ╚════════════════════════════════════════════════════════╝
        """.trimIndent())
    }
    
    private fun leerNumero(): Double {
        return try {
            scanner.nextLine().toDouble()
        } catch (e: NumberFormatException) {
            println("⚠️  Entrada inválida, usando 0")
            0.0
        }
    }
}

// ============================================
// FUNCIÓN MAIN
// ============================================

fun main() {
    val interfaz = InterfazCalculadora()
    interfaz.iniciar()
}

// ============================================
// EJEMPLOS DE USO
// ============================================

/**
 * Función de demostración de las capacidades de la calculadora
 */
fun ejemplosDeUso() {
    println("\n" + "=".repeat(60))
    println("EJEMPLOS DE USO DE LA CALCULADORA CIENTÍFICA")
    println("=".repeat(60))
    
    val calc = CalculadoraCientifica()
    val eval = EvaluadorExpresiones(calc)
    
    // Operaciones básicas con polimorfismo
    println("\n1️⃣ OPERACIONES BÁSICAS (Polimorfismo)")
    println("   Suma de enteros: ${calc.sumar(5, 3)}")
    println("   Suma de decimales: ${calc.sumar(5.5, 3.2)}")
    println("   División: ${calc.dividir(10.0, 2.0)}")
    
    // Funciones trigonométricas
    println("\n2️⃣ FUNCIONES TRIGONOMÉTRICAS")
    val angulo45 = calc.gradosARadianes(45.0)
    println("   sin(45°) = ${calc.seno(angulo45)}")
    println("   cos(45°) = ${calc.coseno(angulo45)}")
    println("   tan(45°) = ${calc.tangente(angulo45)}")
    
    // Potencias y raíces
    println("\n3️⃣ POTENCIAS Y RAÍCES")
    println("   2^8 = ${calc.potencia(2.0, 8.0)}")
    println("   √16 = ${calc.raizCuadrada(16.0)}")
    println("   5! = ${calc.factorial(5)}")
    
    // Logaritmos
    println("\n4️⃣ LOGARITMOS Y EXPONENCIALES")
    println("   log₁₀(100) = ${calc.logaritmoBase10(100.0)}")
    println("   ln(e) = ${calc.logaritmoNatural(Math.E)}")
    println("   e^2 = ${calc.exponencial(2.0)}")
    
    // Expresiones completas
    println("\n5️⃣ EXPRESIONES COMPLETAS")
    println("   2 + 3 * 4 = ${eval.evaluar("2 + 3 * 4")}")
    println("   (5 + 3) * 2 = ${eval.evaluar("(5 + 3) * 2")}")
    println("   sin(pi/4) = ${eval.evaluar("sin(pi/4)")}")
    println("   2^3 + sqrt(16) = ${eval.evaluar("2^3 + sqrt(16)")}")
    
    // Gestión de memoria
    println("\n6️⃣ GESTIÓN DE MEMORIA")
    calc.memoriaGuardar(100.0)
    println("   MS 100 → Memoria: ${calc.memoriaRecuperar()}")
    calc.memoriaAgregar(50.0)
    println("   M+ 50 → Memoria: ${calc.memoriaRecuperar()}")
    calc.memoriaRestar(30.0)
    println("   M- 30 → Memoria: ${calc.memoriaRecuperar()}")
    
    // Manejo de excepciones
    println("\n7️⃣ MANEJO DE EXCEPCIONES")
    try {
        calc.dividir(10.0, 0.0)
    } catch (e: DivisionPorCeroException) {
        println("   ✓ División por cero capturada: ${e.message}")
    }
    
    try {
        calc.raizCuadrada(-4.0)
    } catch (e: OperacionInvalidaException) {
        println("   ✓ Raíz de negativo capturada: ${e.message}")
    }
    
    try {
        calc.logaritmoBase10(-10.0)
    } catch (e: OperacionInvalidaException) {
        println("   ✓ Log de negativo capturado: ${e.message}")
    }
    
    // Historial
    println("\n8️⃣ HISTORIAL DE OPERACIONES")
    calc.obtenerHistorial().takeLast(5).forEach { println("   $it") }
    
    println("\n" + "=".repeat(60))
    println("✅ TODOS LOS PRINCIPIOS DE POO DEMOSTRADOS:")
    println("   • Encapsulamiento: Atributos privados y métodos públicos")
    println("   • Herencia: CalculadoraCientifica extiende Calculadora")
    println("   • Polimorfismo: Sobrecarga de métodos para Int y Double")
    println("   • Abstracción: Interfaces limpias y claras")
    println("=".repeat(60) + "\n")
}

// Descomentar para ejecutar ejemplos
// fun main() {
//     ejemplosDeUso()
// }