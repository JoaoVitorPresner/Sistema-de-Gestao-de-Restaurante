package br.com.restaurante;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api") public class ApiController {
 private final CategoriaRepository categorias; private final ProdutoRepository produtos;
 public ApiController(CategoriaRepository c,ProdutoRepository p){categorias=c;produtos=p;}
 @GetMapping("/categorias") public List<Categoria> categorias(){return categorias.findAll();}
 @PostMapping("/categorias") public ResponseEntity<Categoria> categoria(@Valid @RequestBody Categoria c){c.id=null;return ResponseEntity.status(201).body(categorias.save(c));}
 @GetMapping("/produtos") public List<Produto> produtos(){return produtos.findAll();}
 @PostMapping("/produtos") public ResponseEntity<Produto> produto(@Valid @RequestBody Produto p){
  p.id=null; if(p.categoria==null||p.categoria.id==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Categoria obrigatoria");
  p.categoria=categorias.findById(p.categoria.id).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Categoria inexistente"));
  return ResponseEntity.status(201).body(produtos.save(p));
 }
 @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,String>> invalid(org.springframework.web.bind.MethodArgumentNotValidException ex){return ResponseEntity.badRequest().body(Map.of("erro","Dados invalidos: confira os campos obrigatorios e o preco"));}
}
