INSERT INTO categorias (nombre, descripcion) VALUES
    ('Repuestos Mecánicos', 'Piezas de repuesto para maquinaria y vehículos'),
    ('Insumos y Consumibles', 'Aceites, soldadura, discos de corte, trapo industrial'),
    ('Herramientas', 'Herramientas manuales y accesorios de mecanizado'),
    ('Materia Prima', 'Láminas, tubos, barras de acero, bronce, aluminio')
ON DUPLICATE KEY UPDATE
    descripcion = VALUES(descripcion);
