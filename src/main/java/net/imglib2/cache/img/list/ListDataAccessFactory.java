package net.imglib2.cache.img.list;

import net.imglib2.cache.img.AccessFlags;
import net.imglib2.img.list.access.ListDataAccess;
import net.imglib2.img.list.access.container.DirtyList;
import net.imglib2.img.list.access.container.PlainList;
import net.imglib2.img.list.access.volatiles.container.DirtyVolatileList;
import net.imglib2.img.list.access.volatiles.container.VolatileList;

/**
 * Given {@link AccessFlags} creates a specific {@link ListDataAccess}.
 *
 * @author Tobias Pietzsch
 * @author Igor Pisarev
 */
public class ListDataAccessFactory
{
	@SuppressWarnings( "unchecked" )
	public static < T, A extends ListDataAccess< A > > A get( final AccessFlags ... flags )
	{
		final boolean dirty = AccessFlags.isDirty( flags );
		final boolean volatil = AccessFlags.isVolatile( flags );

		return dirty
				? ( volatil
						? ( A ) new DirtyVolatileList<>( 0, true )
						: ( A ) new DirtyList<>( 0 ) )
				: ( volatil
						? ( A ) new VolatileList<>( 0, true )
						: ( A ) new PlainList<>( 0 ) );
	}
}
