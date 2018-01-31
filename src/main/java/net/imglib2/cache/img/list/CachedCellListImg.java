package net.imglib2.cache.img.list;

import net.imglib2.cache.Cache;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.list.access.container.AbstractList;
import net.imglib2.img.list.cell.LazyCellListImg;

/**
 * A {@link LazyCellListImg} that creates empty Cells lazily when they are accessed
 * and stores (modified) Cells in a disk cache when memory runs full.
 *
 * @param <T>
 *            the pixel type
 * @param <A>
 *            the underlying access type
 *
 * @author Tobias Pietzsch
 * @author Igor Pisarev
 */
public class CachedCellListImg< T, A extends AbstractList< T, A > > extends LazyCellListImg< T, A >
{
	private final Cache< Long, Cell< A > > cache;

	private final A accessType;

	public CachedCellListImg(
			final CellGrid grid,
			final Cache< Long, Cell< A > > cache,
			final A accessType )
	{
		super( grid, cache.unchecked()::get );
		this.cache = cache;
		this.accessType = accessType;
	}

	public Cache< Long, Cell< A > > getCache()
	{
		return cache;
	}

	public A getAccessType()
	{
		return accessType;
	}
}
