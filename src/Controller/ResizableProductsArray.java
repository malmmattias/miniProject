package Controller;

public class ResizableProductsArray<T> {
    private static final int DEFAULT_CAPACITY = 1;
    private Object[] array;
    private int size;

    public ResizableProductsArray() {
        this.array = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public void add(T element) {
        if (size == array.length) {
            resize();
        }
        array[size++] = element;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return (T) array[index];
    }

    public int size() {
        return size;
    }

    private void resize() {
        int newCapacity = array.length * 2;
        Object[] newArray = new Object[newCapacity];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }

    public void overwrite(int i, T element){
        array[i] = element;
    }

}
