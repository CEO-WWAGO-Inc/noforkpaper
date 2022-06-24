package io.papermc.paper.testplugin.behaviors.attack;

import io.papermc.paper.entity.brain.activity.behavior.Behavior;
import io.papermc.paper.entity.brain.memory.MemoryKeyStatus;
import io.papermc.paper.entity.brain.memory.MemoryPair;
import io.papermc.paper.testplugin.TestPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.Collection;
import java.util.List;

public class FollowTarget implements Behavior<Mob> {


    @Override
    public void start(Mob entity) {
    }

    @Override
    public void tick(Mob entity) {
        Entity target = entity.getMemory(TestPlugin.ENTITY_TARGET);
        entity.getPathfinder().moveTo(target.getLocation());
    }

    @Override
    public void stop(Mob entity) {
    }

    @Override
    public int getMinRuntime() {
        return 150;
    }

    @Override
    public int getMaxRuntime() {
        return 250;
    }

    @Override
    public Collection<MemoryPair> getInitialMemoryRequirements() {
        return List.of(new MemoryPair(MemoryKeyStatus.PRESENT, TestPlugin.ENTITY_TARGET));
    }
}
