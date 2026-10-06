import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard/dashboard.component';

describe('DashboardComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [provideZonelessChangeDetection()]
    }).compileComponents();
  });

  it('should create the dashboard', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render inventory metrics', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Total de productos');
    expect(compiled.textContent).toContain('Unidades en stock');
    expect(compiled.textContent).toContain('Destornillador');
  });

  it('should filter products by category and search term', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.componentInstance.categoriaSeleccionada = 'Herramientas';
    fixture.componentInstance.terminoBusqueda = 'taladro';
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Taladro eléctrico');
    expect(compiled.textContent).not.toContain('Nivel de burbuja');
  });
});
