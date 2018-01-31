package net.imglib2.cache.img.list;

import net.imglib2.img.Img;
import net.imglib2.img.list.access.container.AbstractList;
import net.imglib2.img.list.cell.AbstractCellListImg;

/**
 * Populates cells with data.
 *
 * @param <T>
 *            pixel type
 *
 * @author Tobias Pietzsch
 * @author Igor Pisarev
 */
public interface CellListImgLoader< T, A extends AbstractList< T, A > >
{
	/**
	 * Fill the specified cell with data.
	 *
	 * @param cell
	 *            the cell to load. The cell is given as a {@link Img} with
	 *            minimum and maximum reflecting the part of the
	 *            {@link AbstractCellListImg} that is covered.
	 * @throws Exception
	 */
	public void load( SingleCellListImg< T, A > cell ) throws Exception;
}
