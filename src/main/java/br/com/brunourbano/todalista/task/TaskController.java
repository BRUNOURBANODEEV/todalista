package br.com.brunourbano.todalista.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.antlr.v4.runtime.misc.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
        private UUID idUser;
      @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
    
        var idUser = request.getAttribute("idUser");
    
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        var tasks = taskRepository.findByIdUser((UUID) idUser);
        return ResponseEntity.ok(tasks);
    }
        @PostMapping
        public ResponseEntity<?> create(
                @RequestBody TaskModel taskModel,
                HttpServletRequest request) {
        
            Object idUserObj = request.getAttribute("idUser");
        
            if (!(idUserObj instanceof UUID)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Usuário não autenticado");
            }
        
            if (taskModel == null ||
                taskModel.getStartAt() == null ||
                taskModel.getEndAt() == null) {
        
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Datas obrigatórias");
            }
        
            LocalDateTime now = LocalDateTime.now();
        
            if (now.isAfter(taskModel.getStartAt()) ||
                now.isAfter(taskModel.getEndAt())) {
        
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Datas devem ser futuras");
            }
        
            if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Data início > data fim");
            }
        
            taskModel.setIdUser((UUID) idUserObj);
        
            var task = taskRepository.save(taskModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        }
    
       @PutMapping("/{id}")
public ResponseEntity<?> update(
        @PathVariable UUID id,
        @RequestBody TaskModel body,
        HttpServletRequest request) {

    var idUser = request.getAttribute("idUser");


    if (idUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    var taskOpt = taskRepository.findById(id);

    if (taskOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task não encontrada");
    }

    var task = taskOpt.get();

    if (!task.getIdUser().equals(idUser)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    task.setTitle(body.getTitle());
    task.setDescription(body.getDescription());
    task.setStartAt(body.getStartAt());
    task.setEndAt(body.getEndAt());

    var updated = taskRepository.save(task);
    return ResponseEntity.ok(updated);
}

    }
