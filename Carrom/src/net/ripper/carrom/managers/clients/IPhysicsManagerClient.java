package net.ripper.carrom.managers.clients;

import java.util.List;

import net.ripper.carrom.model.CollisionPair;

/**
 * notifies client of physics events All motion stopped event
 * 
 * @author theripper
 * 
 */
public interface IPhysicsManagerClient {
	public void allMotionStopped(List<CollisionPair> collisionPairs);
}
