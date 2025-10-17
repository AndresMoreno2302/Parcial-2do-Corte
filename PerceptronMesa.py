import numpy as np
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import tkinter as tk
from tkinter import ttk
from mesa import Agent, Model
from mesa.time import RandomActivation
from mesa.datacollection import DataCollector
import random

# ============================================
# AGENTES
# ============================================

class DataPoint(Agent):
    """Agente que representa un punto de datos"""
    def __init__(self, unique_id, model, x, y, label):
        super().__init__(unique_id, model)
        self.x = x
        self.y = y
        self.true_label = label
        self.predicted_label = None
        self.correctly_classified = False
    
    def step(self):
        """El punto verifica si está correctamente clasificado"""
        if self.predicted_label is not None:
            self.correctly_classified = (self.predicted_label == self.true_label)


class Perceptron(Agent):
    """Agente que representa el perceptrón"""
    def __init__(self, unique_id, model, learning_rate=0.1):
        super().__init__(unique_id, model)
        self.learning_rate = learning_rate
        # Inicializar pesos aleatorios pequeños
        self.weights = np.random.randn(2) * 0.01
        self.bias = np.random.randn() * 0.01
        self.training_complete = False
        self.current_iteration = 0
    
    def predict(self, x, y):
        """Predice la clase de un punto"""
        z = np.dot(self.weights, [x, y]) + self.bias
        return 1 if z >= 0 else -1
    
    def train_step(self, data_points):
        """Realiza un paso de entrenamiento sobre todos los puntos"""
        total_error = 0
        
        for point in data_points:
            # Predicción
            prediction = self.predict(point.x, point.y)
            point.predicted_label = prediction
            
            # Calcular error
            error = point.true_label - prediction
            
            if error != 0:
                total_error += 1
                # Actualizar pesos
                self.weights[0] += self.learning_rate * error * point.x
                self.weights[1] += self.learning_rate * error * point.y
                self.bias += self.learning_rate * error
        
        return total_error
    
    def get_decision_boundary(self):
        """Retorna los puntos de la línea de decisión"""
        if self.weights[1] != 0:
            x_vals = np.array([-1.5, 1.5])
            y_vals = -(self.weights[0] * x_vals + self.bias) / self.weights[1]
            return x_vals, y_vals
        return None, None
    
    def step(self):
        """Paso de entrenamiento del perceptrón"""
        if not self.training_complete and self.current_iteration < self.model.max_iterations:
            data_points = [agent for agent in self.model.schedule.agents 
                          if isinstance(agent, DataPoint)]
            error = self.train_step(data_points)
            self.current_iteration += 1
            
            if error == 0:
                self.training_complete = True
                self.model.training_finished = True


# ============================================
# MODELO
# ============================================

class PerceptronModel(Model):
    """Modelo que contiene el perceptrón y los puntos de datos"""
    def __init__(self, learning_rate=0.1, max_iterations=100, n_points=50):
        super().__init__()
        self.learning_rate = learning_rate
        self.max_iterations = max_iterations
        self.n_points = n_points
        self.schedule = RandomActivation(self)
        self.training_finished = False
        self.running = True
        
        # Crear perceptrón
        self.perceptron = Perceptron(0, self, learning_rate)
        self.schedule.add(self.perceptron)
        
        # Generar función de separación verdadera (línea aleatoria)
        self.true_slope = random.uniform(-2, 2)
        self.true_intercept = random.uniform(-0.5, 0.5)
        
        # Generar puntos de datos
        self._generate_data_points()
        
        # Data collector para estadísticas
        self.datacollector = DataCollector(
            model_reporters={
                "Accuracy": lambda m: self.calculate_accuracy(),
                "Iteration": lambda m: m.perceptron.current_iteration
            }
        )
    
    def _generate_data_points(self):
        """Genera puntos de datos linealmente separables"""
        for i in range(1, self.n_points + 1):
            x = random.uniform(-1, 1)
            y = random.uniform(-1, 1)
            
            # Clasificar según la línea verdadera
            true_y = self.true_slope * x + self.true_intercept
            label = 1 if y > true_y else -1
            
            point = DataPoint(i, self, x, y, label)
            self.schedule.add(point)
    
    def calculate_accuracy(self):
        """Calcula la precisión del modelo"""
        data_points = [agent for agent in self.schedule.agents 
                      if isinstance(agent, DataPoint)]
        if not data_points:
            return 0
        
        correct = sum(1 for p in data_points if p.correctly_classified)
        return (correct / len(data_points)) * 100
    
    def step(self):
        """Avanza un paso en la simulación"""
        self.datacollector.collect(self)
        self.schedule.step()
    
    def reset_model(self):
        """Reinicia el modelo"""
        self.schedule = RandomActivation(self)
        self.training_finished = False
        self.perceptron = Perceptron(0, self, self.learning_rate)
        self.schedule.add(self.perceptron)
        self._generate_data_points()
        self.datacollector = DataCollector(
            model_reporters={
                "Accuracy": lambda m: self.calculate_accuracy(),
                "Iteration": lambda m: m.perceptron.current_iteration
            }
        )


# ============================================
# INTERFAZ GRÁFICA
# ============================================

class PerceptronGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Simulación de Perceptrón con Agentes MESA")
        self.root.geometry("1200x700")
        
        self.model = None
        self.is_training = False
        
        self._create_widgets()
        self._initialize_model()
    
    def _create_widgets(self):
        """Crea los widgets de la interfaz"""
        # Frame de controles
        control_frame = ttk.Frame(self.root, padding="10")
        control_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Tasa de aprendizaje
        ttk.Label(control_frame, text="Tasa de Aprendizaje:").grid(row=0, column=0, sticky=tk.W, pady=5)
        self.lr_var = tk.DoubleVar(value=0.1)
        self.lr_slider = ttk.Scale(control_frame, from_=0.01, to=1.0, 
                                    variable=self.lr_var, orient=tk.HORIZONTAL, length=300)
        self.lr_slider.grid(row=0, column=1, pady=5)
        self.lr_label = ttk.Label(control_frame, text="0.10")
        self.lr_label.grid(row=0, column=2, padx=5)
        
        # Número de iteraciones
        ttk.Label(control_frame, text="Iteraciones Máximas:").grid(row=1, column=0, sticky=tk.W, pady=5)
        self.iter_var = tk.IntVar(value=100)
        self.iter_slider = ttk.Scale(control_frame, from_=10, to=500, 
                                      variable=self.iter_var, orient=tk.HORIZONTAL, length=300)
        self.iter_slider.grid(row=1, column=1, pady=5)
        self.iter_label = ttk.Label(control_frame, text="100")
        self.iter_label.grid(row=1, column=2, padx=5)
        
        # Actualizar labels
        self.lr_slider.config(command=lambda v: self.lr_label.config(text=f"{float(v):.2f}"))
        self.iter_slider.config(command=lambda v: self.iter_label.config(text=f"{int(float(v))}"))
        
        # Botones
        button_frame = ttk.Frame(control_frame)
        button_frame.grid(row=2, column=0, columnspan=3, pady=20)
        
        self.start_btn = ttk.Button(button_frame, text="Iniciar Entrenamiento", 
                                     command=self.start_training)
        self.start_btn.pack(side=tk.LEFT, padx=5)
        
        self.pause_btn = ttk.Button(button_frame, text="Pausar", 
                                     command=self.pause_training, state=tk.DISABLED)
        self.pause_btn.pack(side=tk.LEFT, padx=5)
        
        self.reset_btn = ttk.Button(button_frame, text="Restablecer", 
                                     command=self.reset_simulation)
        self.reset_btn.pack(side=tk.LEFT, padx=5)
        
        # Información de estado
        self.info_label = ttk.Label(control_frame, text="Estado: Listo para iniciar", 
                                     font=('Arial', 10, 'bold'))
        self.info_label.grid(row=3, column=0, columnspan=3, pady=10)
        
        self.accuracy_label = ttk.Label(control_frame, text="Precisión: 0.00%", 
                                         font=('Arial', 10))
        self.accuracy_label.grid(row=4, column=0, columnspan=3, pady=5)
        
        self.iteration_label = ttk.Label(control_frame, text="Iteración: 0", 
                                          font=('Arial', 10))
        self.iteration_label.grid(row=5, column=0, columnspan=3, pady=5)
        
        # Frame de visualización
        viz_frame = ttk.Frame(self.root, padding="10")
        viz_frame.grid(row=0, column=1, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Crear figura de matplotlib
        self.fig, self.ax = plt.subplots(figsize=(8, 6))
        self.canvas = FigureCanvasTkAgg(self.fig, master=viz_frame)
        self.canvas.get_tk_widget().pack()
        
        # Configurar grid weights
        self.root.columnconfigure(1, weight=1)
        self.root.rowconfigure(0, weight=1)
    
    def _initialize_model(self):
        """Inicializa el modelo"""
        learning_rate = self.lr_var.get()
        max_iterations = self.iter_var.get()
        self.model = PerceptronModel(learning_rate, max_iterations)
        self.update_visualization()
    
    def start_training(self):
        """Inicia el entrenamiento"""
        if not self.is_training:
            self.is_training = True
            self.start_btn.config(state=tk.DISABLED)
            self.pause_btn.config(state=tk.NORMAL)
            self.info_label.config(text="Estado: Entrenando...")
            self.train_step()
    
    def pause_training(self):
        """Pausa el entrenamiento"""
        self.is_training = False
        self.start_btn.config(state=tk.NORMAL)
        self.pause_btn.config(state=tk.DISABLED)
        self.info_label.config(text="Estado: Pausado")
    
    def train_step(self):
        """Realiza un paso de entrenamiento"""
        if self.is_training and not self.model.training_finished:
            if self.model.perceptron.current_iteration < self.model.max_iterations:
                self.model.step()
                self.update_visualization()
                self.update_info()
                self.root.after(50, self.train_step)
            else:
                self.finish_training()
        elif self.model.training_finished:
            self.finish_training()
    
    def finish_training(self):
        """Finaliza el entrenamiento"""
        self.is_training = False
        self.start_btn.config(state=tk.NORMAL)
        self.pause_btn.config(state=tk.DISABLED)
        self.info_label.config(text="Estado: Entrenamiento Completado!")
        self.update_visualization()
        self.update_info()
    
    def reset_simulation(self):
        """Reinicia la simulación"""
        self.is_training = False
        learning_rate = self.lr_var.get()
        max_iterations = self.iter_var.get()
        self.model = PerceptronModel(learning_rate, max_iterations)
        self.start_btn.config(state=tk.NORMAL)
        self.pause_btn.config(state=tk.DISABLED)
        self.info_label.config(text="Estado: Reiniciado - Listo para iniciar")
        self.update_visualization()
        self.update_info()
    
    def update_visualization(self):
        """Actualiza la visualización"""
        self.ax.clear()
        
        # Obtener puntos
        data_points = [agent for agent in self.model.schedule.agents 
                      if isinstance(agent, DataPoint)]
        
        # Dibujar puntos
        for point in data_points:
            if point.predicted_label is None:
                color = 'blue' if point.true_label == 1 else 'orange'
            else:
                color = 'green' if point.correctly_classified else 'red'
            
            marker = 'o' if point.true_label == 1 else 's'
            self.ax.scatter(point.x, point.y, c=color, marker=marker, s=100, 
                           edgecolors='black', linewidth=1.5, alpha=0.7)
        
        # Dibujar línea verdadera (en gris claro)
        x_line = np.array([-1.5, 1.5])
        y_line = self.model.true_slope * x_line + self.model.true_intercept
        self.ax.plot(x_line, y_line, 'gray', linestyle='--', linewidth=2, 
                    label='Frontera Verdadera', alpha=0.5)
        
        # Dibujar línea de decisión del perceptrón
        x_boundary, y_boundary = self.model.perceptron.get_decision_boundary()
        if x_boundary is not None:
            self.ax.plot(x_boundary, y_boundary, 'b-', linewidth=3, 
                        label='Decisión del Perceptrón')
        
        self.ax.set_xlim(-1.5, 1.5)
        self.ax.set_ylim(-1.5, 1.5)
        self.ax.set_xlabel('X', fontsize=12)
        self.ax.set_ylabel('Y', fontsize=12)
        self.ax.set_title('Clasificación del Perceptrón', fontsize=14, fontweight='bold')
        self.ax.legend(loc='upper right')
        self.ax.grid(True, alpha=0.3)
        self.ax.axhline(y=0, color='k', linestyle='-', linewidth=0.5)
        self.ax.axvline(x=0, color='k', linestyle='-', linewidth=0.5)
        
        self.canvas.draw()
    
    def update_info(self):
        """Actualiza la información de estado"""
        accuracy = self.model.calculate_accuracy()
        iteration = self.model.perceptron.current_iteration
        
        self.accuracy_label.config(text=f"Precisión: {accuracy:.2f}%")
        self.iteration_label.config(text=f"Iteración: {iteration}/{self.model.max_iterations}")


# ============================================
# MAIN
# ============================================

if __name__ == "__main__":
    root = tk.Tk()
    app = PerceptronGUI(root)
    root.mainloop()
