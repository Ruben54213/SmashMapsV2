package net.Ruben54213.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

/**
 * Listener zum Verhindern von TNT-Explosionen
 */
public class TNTProtectionListener implements Listener {

    /**
     * Verhindert, dass TNT explodiert (vor der Explosion)
     */
    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        // Prüfen ob es sich um TNT handelt
        if (event.getEntityType() == EntityType.TNT) {
            // TNT-Explosion verhindern
            event.setCancelled(true);
        }
    }

    /**
     * Verhindert Schäden durch TNT-Explosionen (falls die erste Methode nicht greift)
     */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Prüfen ob es sich um TNT handelt
        if (event.getEntityType() == EntityType.TNT) {
            // Explosion komplett verhindern
            event.setCancelled(true);
        }
    }
}