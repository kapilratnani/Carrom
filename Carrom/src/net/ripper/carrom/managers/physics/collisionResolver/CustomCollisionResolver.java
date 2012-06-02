package net.ripper.carrom.managers.physics.collisionResolver;

import net.ripper.carrom.managers.physics.collisionResolver.client.ICustomCollissionResolverClient;
import net.ripper.carrom.model.Piece;

/**
 * Custom collision resolution class with a client which can be notified on
 * collision.
 * 
 * All custom collision resolution classes must inherit and implement the
 * resolveCollision function and notify the client if required
 * 
 * @author theripper
 * 
 */
public abstract class CustomCollisionResolver {
	ICustomCollissionResolverClient client;

	/**
	 * 
	 * @param client
	 *            null if no client is required
	 */
	protected CustomCollisionResolver(ICustomCollissionResolverClient client) {
		this.client = client;
	}

	protected void notifyClient(Piece pieceA, Piece pieceB) {
		if (client != null)
			client.collisionHappened(pieceA, pieceB);
	}

	public abstract void resolveCollision(Piece pieceA, Piece pieceB);
}
