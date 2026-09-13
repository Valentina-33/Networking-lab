# Networking

https://developer.mozilla.org/en-US/docs/Web/HTTP

Separación de enter entre el encabezado y el cuerpo

Pasos:
Creo un socket para escuchar en un puerto
Alguien quiere conectarse le doy el puerto
Creo un socket adicional para escribir/leer (bidireccional) - Viva para siempre hasta que alguien lo mate
Usa TCP porque siempre la deja abierta y HTTP la cierra

Nosotros vamos a crearlo sin hilos

<img width="1446" height="275" alt="Screenshot 2026-09-01 094544" src="https://github.com/user-attachments/assets/46e83140-6d6d-41b9-95cd-f1bb9a7e4b0b" />

<img width="1412" height="843" alt="image" src="https://github.com/user-attachments/assets/f041d0d4-e4c9-4eed-b21f-87c9e32cda58" />
