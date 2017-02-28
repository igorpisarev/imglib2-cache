package net.imglib2.cache;

public interface UncheckedLoadingCache< K, V > extends AbstractCache< K, V >, CacheLoader< K, V >
{
	@Override
	V get( K key );
}
