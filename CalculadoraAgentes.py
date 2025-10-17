import re
from mesa import Agent, Model
from mesa.time import BaseScheduler
import tkinter as tk
from tkinter import ttk, scrolledtext
from typing import Optional, List, Dict, Any
import ast
import operator

# ============================================
# SISTEMA DE MENSAJES
# ============================================

class Message:
    """Representa un mensaje entre agentes"""
    def __init__(self, sender, receiver, content, msg_type="operation"):
        self.sender = sender
        self.receiver = receiver
        self.content = content
        self.msg_type = msg_type
        self.timestamp = None
    
    def __repr__(self):
        return f"Message(from={self.sender}, to={self.receiver}, type={self.msg_type})"


class MessageBroker:
    """Sistema de mensajería entre agentes"""
    def __init__(self):
        self.message_queue = []
        self.message_history = []
        self.log_enabled = True
    
    def send_message(self, message: Message):
        """Envía un mensaje al queue"""
        self.message_queue.append(message)
        if self.log_enabled:
            self.message_history.append(message)
    
    def get_messages_for(self, agent_id: str) -> List[Message]:
        """Obtiene y elimina mensajes para un agente específico"""
        messages = [m for m in self.message_queue if m.receiver == agent_id]
        self.message_queue = [m for m in self.message_queue if m.receiver != agent_id]
        return messages
    
    def clear_queue(self):
        """Limpia el queue de mensajes"""
        self.message_queue.clear()
    
    def get_history(self):
        """Retorna el historial de mensajes"""
        return self.message_history.copy()
    
    def clear_history(self):
        """Limpia el historial"""
        self.message_history.clear()


# ============================================
# AGENTES DE OPERACIÓN
# ============================================

class OperationAgent(Agent):
    """Agente base para operaciones matemáticas"""
    def __init__(self, unique_id, model, operation_name, operation_func, symbol):
        super().__init__(unique_id, model)
        self.operation_name = operation_name
        self.operation_func = operation_func
        self.symbol = symbol
        self.operations_performed = 0
    
    def perform_operation(self, operand1: float, operand2: float) -> float:
        """Realiza la operación matemática"""
        try:
            result = self.operation_func(operand1, operand2)
            self.operations_performed += 1
            
            # Log de la operación
            log_msg = f"[{self.operation_name}] {operand1} {self.symbol} {operand2} = {result}"
            self.model.add_log(log_msg)
            
            return result
        except Exception as e:
            error_msg = f"[{self.operation_name}] Error: {str(e)}"
            self.model.add_log(error_msg)
            raise
    
    def step(self):
        """Procesa mensajes recibidos"""
        messages = self.model.broker.get_messages_for(self.unique_id)
        
        for msg in messages:
            if msg.msg_type == "operation":
                operand1, operand2 = msg.content['operands']
                result = self.perform_operation(operand1, operand2)
                
                # Enviar resultado de vuelta
                response = Message(
                    sender=self.unique_id,
                    receiver=msg.sender,
                    content={'result': result},
                    msg_type="result"
                )
                self.model.broker.send_message(response)


class SumaAgent(OperationAgent):
    """Agente que maneja sumas"""
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "SUMA", operator.add, "+")


class RestaAgent(OperationAgent):
    """Agente que maneja restas"""
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "RESTA", operator.sub, "-")


class MultiplicacionAgent(OperationAgent):
    """Agente que maneja multiplicaciones"""
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "MULTIPLICACIÓN", operator.mul, "*")


class DivisionAgent(OperationAgent):
    """Agente que maneja divisiones"""
    def __init__(self, unique_id, model):
        def safe_div(a, b):
            if b == 0:
                raise ValueError("División por cero no permitida")
            return a / b
        super().__init__(unique_id, model, "DIVISIÓN", safe_div, "/")


class PotenciaAgent(OperationAgent):
    """Agente que maneja potencias"""
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "POTENCIA", operator.pow, "**")


# ============================================
# AGENTE DE ENTRADA/SALIDA
# ============================================

class IOAgent(Agent):
    """Agente que maneja entrada/salida y coordina las operaciones"""
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model)
        self.current_expression = None
        self.result = None
        self.processing = False
        
        # Mapeo de operadores a agentes
        self.operator_map = {
            '+': 'suma_agent',
            '-': 'resta_agent',
            '*': 'multiplicacion_agent',
            '/': 'division_agent',
            '**': 'potencia_agent'
        }
    
    def process_expression(self, expression: str):
        """Procesa una expresión matemática"""
        self.current_expression = expression
        self.processing = True
        self.model.add_log(f"\n{'='*60}")
        self.model.add_log(f"[IO] Nueva expresión: {expression}")
        self.model.add_log(f"{'='*60}")
        
        try:
            # Limpiar y validar expresión
            expression = expression.replace(' ', '')
            
            # Tokenizar la expresión
            tokens = self.tokenize(expression)
            self.model.add_log(f"[IO] Tokens: {tokens}")
            
            # Evaluar usando precedencia de operadores
            result = self.evaluate_expression(tokens)
            
            self.result = result
            self.model.add_log(f"[IO] ✓ Resultado final: {result}")
            self.model.add_log(f"{'='*60}\n")
            
            return result
            
        except Exception as e:
            error_msg = f"[IO] ✗ Error al procesar expresión: {str(e)}"
            self.model.add_log(error_msg)
            self.result = None
            raise
        finally:
            self.processing = False
    
    def tokenize(self, expression: str) -> List[str]:
        """Convierte la expresión en tokens"""
        # Patrón para números (incluyendo decimales y negativos) y operadores
        pattern = r'(\d+\.?\d*|\+|\-|\*\*|\*|\/|\(|\))'
        tokens = re.findall(pattern, expression)
        
        # Manejar números negativos al inicio o después de operadores
        processed_tokens = []
        i = 0
        while i < len(tokens):
            token = tokens[i]
            
            # Si es un '-' al inicio o después de operador/paréntesis
            if token == '-' and (i == 0 or tokens[i-1] in ['(', '+', '-', '*', '/', '**']):
                if i + 1 < len(tokens) and tokens[i+1].replace('.', '').isdigit():
                    processed_tokens.append('-' + tokens[i+1])
                    i += 2
                    continue
            
            processed_tokens.append(token)
            i += 1
        
        return processed_tokens
    
    def evaluate_expression(self, tokens: List[str]) -> float:
        """Evalúa la expresión respetando precedencia de operadores"""
        # Manejar paréntesis primero
        tokens = self.handle_parentheses(tokens)
        
        # Orden de precedencia: potencia -> multiplicación/división -> suma/resta
        # Potencias
        tokens = self.evaluate_operator(tokens, ['**'])
        
        # Multiplicación y división (de izquierda a derecha)
        tokens = self.evaluate_operator(tokens, ['*', '/'])
        
        # Suma y resta (de izquierda a derecha)
        tokens = self.evaluate_operator(tokens, ['+', '-'])
        
        # Debe quedar un solo token con el resultado
        if len(tokens) == 1:
            return float(tokens[0])
        else:
            raise ValueError(f"Error en evaluación: tokens restantes = {tokens}")
    
    def handle_parentheses(self, tokens: List[str]) -> List[str]:
        """Maneja expresiones entre paréntesis"""
        while '(' in tokens:
            # Encontrar el último paréntesis de apertura
            start = -1
            for i, token in enumerate(tokens):
                if token == '(':
                    start = i
            
            if start == -1:
                break
            
            # Encontrar el paréntesis de cierre correspondiente
            end = -1
            for i in range(start + 1, len(tokens)):
                if tokens[i] == ')':
                    end = i
                    break
            
            if end == -1:
                raise ValueError("Paréntesis no balanceados")
            
            # Evaluar la sub-expresión
            sub_tokens = tokens[start+1:end]
            self.model.add_log(f"[IO] Evaluando paréntesis: {' '.join(sub_tokens)}")
            result = self.evaluate_expression(sub_tokens)
            
            # Reemplazar la sub-expresión con su resultado
            tokens = tokens[:start] + [str(result)] + tokens[end+1:]
        
        return tokens
    
    def evaluate_operator(self, tokens: List[str], operators: List[str]) -> List[str]:
        """Evalúa operadores de la misma precedencia de izquierda a derecha"""
        i = 0
        while i < len(tokens):
            if tokens[i] in operators:
                if i == 0 or i == len(tokens) - 1:
                    raise ValueError(f"Operador {tokens[i]} en posición inválida")
                
                operator = tokens[i]
                operand1 = float(tokens[i-1])
                operand2 = float(tokens[i+1])
                
                # Solicitar operación al agente correspondiente
                result = self.request_operation(operator, operand1, operand2)
                
                # Reemplazar los tres tokens con el resultado
                tokens = tokens[:i-1] + [str(result)] + tokens[i+2:]
                # No incrementar i porque ahora tenemos un token menos
            else:
                i += 1
        
        return tokens
    
    def request_operation(self, operator: str, operand1: float, operand2: float) -> float:
        """Solicita una operación a un agente específico"""
        agent_id = self.operator_map.get(operator)
        
        if not agent_id:
            raise ValueError(f"Operador no soportado: {operator}")
        
        # Crear mensaje para el agente de operación
        msg = Message(
            sender=self.unique_id,
            receiver=agent_id,
            content={'operands': (operand1, operand2)},
            msg_type="operation"
        )
        
        self.model.broker.send_message(msg)
        self.model.add_log(f"[IO] → Enviando a {agent_id}: {operand1} {operator} {operand2}")
        
        # Activar el agente para que procese el mensaje
        agent = self.model.schedule._agents.get(agent_id)
        if agent:
            agent.step()
        
        # Esperar respuesta
        response = self.model.broker.get_messages_for(self.unique_id)
        if response and len(response) > 0:
            result = response[0].content['result']
            self.model.add_log(f"[IO] ← Recibido de {agent_id}: {result}")
            return result
        else:
            raise RuntimeError(f"No se recibió respuesta del agente {agent_id}")
    
    def step(self):
        """El IOAgent no tiene step automático"""
        pass


# ============================================
# MODELO
# ============================================

class CalculatorModel(Model):
    """Modelo que coordina todos los agentes de la calculadora"""
    def __init__(self):
        super().__init__()
        self.schedule = BaseScheduler(self)
        self.broker = MessageBroker()
        self.log_messages = []
        
        # Crear agentes de operación
        self.suma_agent = SumaAgent("suma_agent", self)
        self.resta_agent = RestaAgent("resta_agent", self)
        self.multiplicacion_agent = MultiplicacionAgent("multiplicacion_agent", self)
        self.division_agent = DivisionAgent("division_agent", self)
        self.potencia_agent = PotenciaAgent("potencia_agent", self)
        
        # Crear agente de E/S
        self.io_agent = IOAgent("io_agent", self)
        
        # Agregar agentes al scheduler
        self.schedule.add(self.suma_agent)
        self.schedule.add(self.resta_agent)
        self.schedule.add(self.multiplicacion_agent)
        self.schedule.add(self.division_agent)
        self.schedule.add(self.potencia_agent)
        self.schedule.add(self.io_agent)
    
    def add_log(self, message: str):
        """Agrega un mensaje al log"""
        self.log_messages.append(message)
    
    def get_logs(self) -> List[str]:
        """Obtiene todos los logs"""
        return self.log_messages.copy()
    
    def clear_logs(self):
        """Limpia los logs"""
        self.log_messages.clear()
    
    def calculate(self, expression: str) -> float:
        """Calcula una expresión matemática"""
        self.clear_logs()
        self.broker.clear_history()
        result = self.io_agent.process_expression(expression)
        return result
    
    def get_statistics(self) -> Dict[str, int]:
        """Obtiene estadísticas de operaciones realizadas"""
        return {
            "Sumas": self.suma_agent.operations_performed,
            "Restas": self.resta_agent.operations_performed,
            "Multiplicaciones": self.multiplicacion_agent.operations_performed,
            "Divisiones": self.division_agent.operations_performed,
            "Potencias": self.potencia_agent.operations_performed
        }
    
    def reset_statistics(self):
        """Reinicia las estadísticas"""
        self.suma_agent.operations_performed = 0
        self.resta_agent.operations_performed = 0
        self.multiplicacion_agent.operations_performed = 0
        self.division_agent.operations_performed = 0
        self.potencia_agent.operations_performed = 0


# ============================================
# INTERFAZ GRÁFICA
# ============================================

class CalculatorGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Calculadora Basada en Agentes - MESA")
        self.root.geometry("900x700")
        self.root.configure(bg='#2c3e50')
        
        self.model = CalculatorModel()
        
        self._create_widgets()
    
    def _create_widgets(self):
        """Crea todos los widgets de la interfaz"""
        # Frame principal
        main_frame = tk.Frame(self.root, bg='#2c3e50')
        main_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Título
        title_label = tk.Label(
            main_frame, 
            text="🤖 CALCULADORA CON AGENTES 🤖",
            font=('Arial', 20, 'bold'),
            bg='#2c3e50',
            fg='#ecf0f1'
        )
        title_label.pack(pady=10)
        
        # Frame de entrada
        input_frame = tk.Frame(main_frame, bg='#34495e', relief=tk.RAISED, bd=2)
        input_frame.pack(fill=tk.X, pady=10, padx=5)
        
        tk.Label(
            input_frame,
            text="Expresión:",
            font=('Arial', 12, 'bold'),
            bg='#34495e',
            fg='#ecf0f1'
        ).pack(side=tk.LEFT, padx=10, pady=10)
        
        self.expression_entry = tk.Entry(
            input_frame,
            font=('Courier New', 14),
            width=40,
            bg='#ecf0f1',
            fg='#2c3e50'
        )
        self.expression_entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5, pady=10)
        self.expression_entry.bind('<Return>', lambda e: self.calculate())
        
        # Botones
        btn_frame = tk.Frame(main_frame, bg='#2c3e50')
        btn_frame.pack(pady=5)
        
        self.calc_btn = tk.Button(
            btn_frame,
            text="CALCULAR",
            command=self.calculate,
            font=('Arial', 12, 'bold'),
            bg='#27ae60',
            fg='white',
            width=15,
            cursor='hand2'
        )
        self.calc_btn.pack(side=tk.LEFT, padx=5)
        
        self.clear_btn = tk.Button(
            btn_frame,
            text="LIMPIAR",
            command=self.clear_all,
            font=('Arial', 12, 'bold'),
            bg='#e74c3c',
            fg='white',
            width=15,
            cursor='hand2'
        )
        self.clear_btn.pack(side=tk.LEFT, padx=5)
        
        # Frame de resultado
        result_frame = tk.Frame(main_frame, bg='#34495e', relief=tk.RAISED, bd=2)
        result_frame.pack(fill=tk.X, pady=10, padx=5)
        
        tk.Label(
            result_frame,
            text="Resultado:",
            font=('Arial', 12, 'bold'),
            bg='#34495e',
            fg='#ecf0f1'
        ).pack(side=tk.LEFT, padx=10, pady=10)
        
        self.result_label = tk.Label(
            result_frame,
            text="---",
            font=('Courier New', 16, 'bold'),
            bg='#ecf0f1',
            fg='#27ae60',
            relief=tk.SUNKEN,
            width=35
        )
        self.result_label.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5, pady=10)
        
        # Notebook para pestañas
        notebook = ttk.Notebook(main_frame)
        notebook.pack(fill=tk.BOTH, expand=True, pady=10)
        
        # Pestaña de Log
        log_frame = tk.Frame(notebook, bg='#ecf0f1')
        notebook.add(log_frame, text="📋 Log de Comunicación")
        
        tk.Label(
            log_frame,
            text="Comunicación entre Agentes:",
            font=('Arial', 11, 'bold'),
            bg='#ecf0f1',
            fg='#2c3e50'
        ).pack(anchor=tk.W, padx=10, pady=5)
        
        self.log_text = scrolledtext.ScrolledText(
            log_frame,
            font=('Courier New', 10),
            bg='#2c3e50',
            fg='#2ecc71',
            height=15,
            wrap=tk.WORD
        )
        self.log_text.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)
        
        # Pestaña de estadísticas
        stats_frame = tk.Frame(notebook, bg='#ecf0f1')
        notebook.add(stats_frame, text="📊 Estadísticas")
        
        self.stats_text = scrolledtext.ScrolledText(
            stats_frame,
            font=('Courier New', 12),
            bg='#34495e',
            fg='#ecf0f1',
            height=15,
            wrap=tk.WORD
        )
        self.stats_text.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Frame de ayuda
        help_frame = tk.Frame(main_frame, bg='#34495e', relief=tk.RAISED, bd=2)
        help_frame.pack(fill=tk.X, pady=5, padx=5)
        
        help_text = "Operadores: + - * / ** ( )  |  Ejemplo: 2 + 3 * (4 - 1) ** 2"
        tk.Label(
            help_frame,
            text=help_text,
            font=('Arial', 9),
            bg='#34495e',
            fg='#bdc3c7'
        ).pack(pady=5)
    
    def calculate(self):
        """Calcula la expresión ingresada"""
        expression = self.expression_entry.get().strip()
        
        if not expression:
            self.result_label.config(text="⚠ Ingrese una expresión", fg='#e74c3c')
            return
        
        try:
            result = self.model.calculate(expression)
            self.result_label.config(text=f"{result:.6f}".rstrip('0').rstrip('.'), fg='#27ae60')
            
            # Actualizar log
            self.update_log()
            
            # Actualizar estadísticas
            self.update_statistics()
            
        except Exception as e:
            self.result_label.config(text=f"✗ Error: {str(e)}", fg='#e74c3c')
            self.log_text.insert(tk.END, f"\n❌ ERROR: {str(e)}\n")
    
    def update_log(self):
        """Actualiza el área de log"""
        self.log_text.delete(1.0, tk.END)
        logs = self.model.get_logs()
        
        for log in logs:
            self.log_text.insert(tk.END, log + "\n")
        
        self.log_text.see(tk.END)
    
    def update_statistics(self):
        """Actualiza las estadísticas"""
        self.stats_text.delete(1.0, tk.END)
        stats = self.model.get_statistics()
        
        total = sum(stats.values())
        
        self.stats_text.insert(tk.END, "═" * 50 + "\n")
        self.stats_text.insert(tk.END, "  ESTADÍSTICAS DE OPERACIONES\n")
        self.stats_text.insert(tk.END, "═" * 50 + "\n\n")
        
        for operation, count in stats.items():
            percentage = (count / total * 100) if total > 0 else 0
            bar = "█" * int(percentage / 2)
            self.stats_text.insert(tk.END, f"  {operation:20} : {count:3} [{bar}] {percentage:.1f}%\n")
        
        self.stats_text.insert(tk.END, f"\n  {'TOTAL':20} : {total:3}\n")
        self.stats_text.insert(tk.END, "═" * 50 + "\n")
    
    def clear_all(self):
        """Limpia toda la interfaz"""
        self.expression_entry.delete(0, tk.END)
        self.result_label.config(text="---", fg='#27ae60')
        self.log_text.delete(1.0, tk.END)
        self.model.reset_statistics()
        self.update_statistics()


# ============================================
# MAIN
# ============================================

if __name__ == "__main__":
    root = tk.Tk()
    app = CalculatorGUI(root)
    root.mainloop()
