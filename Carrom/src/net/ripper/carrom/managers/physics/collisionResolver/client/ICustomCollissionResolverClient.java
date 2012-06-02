package net.ripper.carrom.managers.physics.collisionResolver.client;

import net.ripper.carrom.model.Piece;

/**
 * Client should implement this interface to get
 * collision event notification
 * @author theripper
 *
 */
public interface ICustomCollissionResolverClient {
	public void collisionHappened(Piece pieceA, Piece pieceB);
}
