# Plan de Branching — Sanos y Salvos
## Estrategia Git Flow · Fullstack III · DuocUC 2026

**Estudiantes:** Bastián Martínez Flores · Maicol Saldivia Silva

---

## 1. Estrategia Elegida: Git Flow

Se adoptó **Git Flow** como estrategia de branching por ser adecuada para proyectos con releases claramente definidos (Parcial 1 y Parcial 2) y trabajo en equipo de 2 personas.

---

## 2. Estructura de ramas

```
main
│  └── Código estable y aprobado (solo recibe merges desde release/*)
│
develop
│  └── Rama de integración continua. Todo el trabajo se integra aquí primero.
│
feature/*       ← trabajo de nuevas funcionalidades
hotfix/*        ← correcciones urgentes sobre main
release/*       ← preparación de entregables
```

### Ramas de largo plazo (persistentes)
- **main**: código en estado de producción. Solo recibe merges desde `release/*` o `hotfix/*`. Nunca se trabaja directamente en esta rama.
- **develop**: rama base de trabajo. Todas las features se integran aquí y se validan antes de subir a main.

### Ramas de corto plazo (se eliminan al hacer merge)
- **feature/**: una por funcionalidad o microservicio
- **hotfix/**: correcciones urgentes que van directo a main
- **release/**: preparación del entregable (ajuste de versiones, documentación final)

---

## 3. Ramas creadas en el proyecto

```
main
develop
feature/ms-gestion-mascotas
feature/ms-geolocalizacion
feature/ms-motor-coincidencias
feature/ms-usuarios-entidades
feature/bff-circuit-breaker
feature/frontend-react
feature/patrones-diseno
feature/pruebas-unitarias
feature/documentacion
release/parcial-1
release/parcial-2
```

---

## 4. Flujo de trabajo del equipo

### Iniciar una nueva feature

```bash
# Desde develop, crear la rama de feature
git checkout develop
git pull origin develop
git checkout -b feature/ms-gestion-mascotas

# Trabajar en la feature...
git add .
git commit -m "feat(ms-mascotas): agrega Factory Method para Alerta"

# Subir la rama y crear Pull Request hacia develop
git push origin feature/ms-gestion-mascotas
```

### Integrar la feature a develop (vía Pull Request)

1. En GitHub: crear PR de `feature/ms-gestion-mascotas` → `develop`
2. El otro integrante revisa y aprueba
3. Se hace merge (squash o merge commit)
4. Se elimina la rama feature

### Preparar un release (entregable)

```bash
git checkout develop
git checkout -b release/parcial-2

# Ajustes finales, versión, README...
git commit -m "chore: prepara release parcial 2"

# Merge a main Y a develop
git checkout main
git merge release/parcial-2
git tag v2.0.0

git checkout develop
git merge release/parcial-2

git branch -d release/parcial-2
```

---

## 5. Convención de mensajes de commit

Se siguió el estándar **Conventional Commits**:

```
tipo(scope): descripcion corta

Tipos válidos:
  feat     → nueva funcionalidad
  fix      → corrección de bug
  docs     → solo cambios en documentación
  style    → formato (sin cambio de lógica)
  refactor → refactorización sin cambio funcional
  test     → agregar o corregir pruebas
  chore    → tareas de configuración, build, etc.
```

### Ejemplos reales del proyecto

```
feat(ms-mascotas): implementa patron Repository con MySQL
feat(ms-mascotas): agrega Factory Method para AlertaExtravio y AlertaHallazgo
feat(ms-mascotas): implementa Observer con EventBus para cambios de estado
feat(bff): implementa Circuit Breaker con estados CLOSED/OPEN/HALF_OPEN
feat(ms-motor): algoritmo Haversine para calculo de distancia geografica
feat(ms-usuarios): autenticacion JWT con roles RBAC
fix(vite): actualiza proxy a puerto 3005 del BFF
test(ms-mascotas): agrega pruebas unitarias Factory y Observer
test(ms-motor): agrega pruebas unitarias algoritmo Haversine
docs: agrega documento de patrones y arquetipos
docs: agrega plan de branching GitFlow
chore: setup base de datos MySQL con datos de ejemplo
```

---

## 6. Resolución de conflictos

### Conflicto registrado: merge de feature/bff con develop

Durante la integración del BFF, `develop` había recibido un cambio en `api.js` del frontend que actualizaba el puerto proxy. La feature del BFF también modificaba ese archivo. Pasos realizados para resolver:

```bash
git checkout develop
git merge feature/bff-circuit-breaker
# CONFLICT: frontend/vite.config.js
```

Resolución manual: se abrió el archivo en VS Code, se eligió el bloque del BFF (puerto 3005) y se eliminó el bloque obsoleto (puerto 3000). Se verificó que el proxy funcionara:

```bash
git add frontend/vite.config.js
git commit -m "fix: resuelve conflicto en vite.config al integrar BFF"
```

---

## 7. Diagrama de flujo Git Flow del proyecto

```
main         ─────────────────────────────○ v1.0.0 ──────────────────○ v2.0.0
                                          │                           │
release/    ──────────────────────────○───┘              ○───────────┘
                                    ↑                  ↑
develop     ──○────○────○────○────○─┘──○────○────○───○─┘
              │    │    │    │           │    │    │
feature/  ms-mascotas  bff-cb       ms-motor  tests  docs
          ms-geo                    ms-usuarios
          ms-motor
          frontend
```

---

## 8. Repositorios GitHub del proyecto

Ver archivo `repositorios.txt` para los enlaces actualizados.

---

*Documento preparado para Evaluación Parcial N°2 — DSY1106 Desarrollo Fullstack III*
