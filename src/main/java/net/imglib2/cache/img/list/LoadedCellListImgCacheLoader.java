package net.imglib2.cache.img.list;

import net.imglib2.Dirty;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.img.AccessFlags;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.list.access.ListDataAccess;
import net.imglib2.img.list.access.container.AbstractList;
import net.imglib2.util.Intervals;

/**
 * A {@link CacheLoader} that produces cells of {@link ListDataAccess} type
 * {@code A} and uses a {@link CellListImgLoader} to populate them with data.
 * <p>
 * Usually, {@link LoadedCellListImgCacheLoader} should be created through static
 * helper methods {@link #get(CellGrid, CellLoader, AccessFlags...)}
 * to get the desired type and dirty/volatile variant.
 * </p>
 *
 * @param <T>
 *            pixel type
 * @param <A>
 *            access type
 *
 * @author Tobias Pietzsch
 * @author Igor Pisarev
 */
public class LoadedCellListImgCacheLoader< T, A extends AbstractList< T, A > > implements CacheLoader< Long, Cell< A > >
{
	private final CellGrid grid;

	private final A creator;

	private final ListDataAccessWrapper< T, A, A > wrapper;

	private final CellListImgLoader< T, A > loader;

	public LoadedCellListImgCacheLoader(
			final CellGrid grid,
			final A creator,
			final ListDataAccessWrapper< T, A, A > wrapper,
			final CellListImgLoader< T, A > loader )
	{
		this.grid = grid;
		this.creator = creator;
		this.wrapper = wrapper;
		this.loader = loader;
	}

	@Override
	public Cell< A > get( final Long key ) throws Exception
	{
		final long index = key;
		final long[] cellMin = new long[ grid.numDimensions() ];
		final int[] cellDims = new int[ grid.numDimensions() ];
		grid.getCellDimensions( index, cellMin, cellDims );
		final long numEntities = Intervals.numElements( cellDims );
		final A list = creator.createList( ( int ) numEntities );
		final SingleCellListImg< T, A > img = new SingleCellListImg<>( cellDims, cellMin, wrapper.wrap( list ), wrapper.wrapDirty( list ) );
		loader.load( img );
		return new Cell<>( cellDims, cellMin, list );
	}

	public static < T, A extends AbstractList< T, A > > LoadedCellListImgCacheLoader< T, A > get(
			final CellGrid grid,
			final CellListImgLoader< T, A > loader,
			final AccessFlags ... flags )
	{
		final A creator = ListDataAccessFactory.get( flags );
		final ListDataAccessWrapper< T, A, A > wrapper = getWrapper( flags );
		return creator == null ? null : new LoadedCellListImgCacheLoader<>( grid, creator, wrapper, loader );
	}

	@SuppressWarnings( "unchecked" )
	static < T, A extends AbstractList< T, A > > ListDataAccessWrapper< T, A, A > getWrapper( final AccessFlags ... flags )
	{
		final boolean dirty = AccessFlags.isDirty( flags );
		return dirty
				? ( ListDataAccessWrapper< T, A, A > ) new ListAccessWrapper<>()
				: ( ListDataAccessWrapper< T, A, A > ) new PassThrough<>();
	}

	/**
	 * Wraps an {@link ListDataAccess} of type {@code A} as another
	 * {@link ListDataAccess}. This is used to strip the dirty flag off
	 * {@link Dirty} accesses for initially populating a cell with data
	 * (otherwise the cell would immediately be marked dirty).
	 * <p>
	 * Additionally, {@link #wrapDirty(ListDataAccess)} provides access to the
	 * dirty flag (if any) to be able to selectively mark cells as dirty from a
	 * {@link CellLoader}.
	 * </p>
	 */
	public static interface ListDataAccessWrapper< T, A extends AbstractList< T, A >, W extends AbstractList< T, W > >
	{
		W wrap( A access );

		Dirty wrapDirty( A access );
	}

	static class PassThrough< T, A extends AbstractList< T, A > > implements ListDataAccessWrapper< T, A, A >
	{
		@Override
		public A wrap( final A access )
		{
			return access;
		}

		@Override
		public Dirty wrapDirty( final A access )
		{
			return new Dirty()
			{
				@Override
				public boolean isDirty()
				{
					return false;
				}

				@Override
				public void setDirty()
				{}
			};
		}
	};

	static class ListAccessWrapper< T, A extends AbstractList< T, A > & Dirty > implements ListDataAccessWrapper< T, A, A >
	{
		@Override
		public A wrap( final A access )
		{
			return access;
		}

		@Override
		public Dirty wrapDirty( final A access )
		{
			return access;
		}
	}
}
