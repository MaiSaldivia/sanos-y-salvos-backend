package com.sanosysalvos.mascotas.pattern.observer;

import com.sanosysalvos.mascotas.model.Mascota;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MascotaEventListener {

    @EventListener
    public void alReportarMascota(MascotaReportadaEvent event) {
        Mascota m = event.getMascota();
        System.out.println(String.format("[OBSERVER][NUEVO REPORTE] %s: \"%s\" en %s", 
                m.getEstado(), m.getNombre(), m.getSector()));
    }

    @EventListener
    public void alCambiarEstado(MascotaEstadoCambiadoEvent event) {
        System.out.println(String.format("[OBSERVER][ESTADO] Mascota %s: %s → %s", 
                event.getIdMascota(), event.getEstadoAnterior(), event.getEstadoNuevo()));
    }

    @EventListener
    public void alReunificarMascota(MascotaReunificadaEvent event) {
        Mascota m = event.getMascota();
        String identifier = m.getNombre() != null ? m.getNombre() : m.getIdMascota();
        System.out.println(String.format("[OBSERVER][REUNION] Mascota \"%s\" reunificada con su dueño. ✅", identifier));
    }
}
