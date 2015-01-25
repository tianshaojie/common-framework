package io.github.jsbd.common.lang;

import java.io.Serializable;

/**
 * 分页器
 */
public class Paginator implements Serializable, Cloneable {

    private static final long serialVersionUID = 2611344617978167939L;

    /**
     * 默认每页20条
     */
    public static final int DEFAULT_ITEMS_PER_PAGE = 20;

    /**
     * 滑动窗口默认的大小(10)。
     */
    public static final int DEFAULT_SLIDER_SIZE = 10;
    public static final int UNKNOWN_ITEMS = 0;

    /**
     * 当前页码，从0开始
     */
    private int page;// 0-based

    /**
     * 总记录数
     */
    private int items;

    /**
     * 总页数
     */
    private int pageCount;

    /**
     * 每页记录条数
     */
    private int itemsPerPage;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * 当前页码从0开始，总条数为无限大，每页20条
     */
    public Paginator() {
        this(DEFAULT_ITEMS_PER_PAGE);
    }

    /**
     * 当前页码从0开始，总条数为无限大，每页<code>itemsPerPage</code>条
     *
     * @param itemsPerPage 每页记录条数
     */
    public Paginator(int itemsPerPage) {
        this(itemsPerPage, UNKNOWN_ITEMS);
    }

    /**
     * 当前页码从0开始，总条数为<code>items</code>，每页<code>itemsPerPage</code>条
     *
     * @param itemsPerPage 每页记录条数
     * @param items        总记录数
     */
    public Paginator(int itemsPerPage, int items) {
        this.items = (items >= 0) ? items : 0;
        this.itemsPerPage = (itemsPerPage > 0) ? itemsPerPage
                : DEFAULT_ITEMS_PER_PAGE;
        this.page = calcPage(0);
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getPages() {
        return (int) Math.ceil((double) items / itemsPerPage);
    }

    /**
     * 获取当前页
     *
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * 设置当前页
     *
     * @param page 当前页码
     * @return
     */
    public int setPage(int page) {
        return (this.page = calcPage(page));
    }

    /**
     * 获取总记录数
     *
     * @return
     */
    public int getItems() {
        return items;
    }

    /**
     * 设置总记录数，并重新计算当前页码以确保其不超过总页数
     *
     * @param items 总记录数
     * @return
     */
    public int setItems(int items) {
        this.items = (items >= 0) ? items : 0;
        setPage(page);
        setPageCount(getPages());
        return this.items;
    }

    /**
     * 获取每页记录数
     *
     * @return
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     * 设置每页记录数，并重新计算当前页码
     *
     * @param itemsPerPage 每页记录数
     * @return
     */
    public int setItemsPerPage(int itemsPerPage) {
        int tmp = this.itemsPerPage;

        this.itemsPerPage = (itemsPerPage > 0) ? itemsPerPage
                : DEFAULT_ITEMS_PER_PAGE;

        if (page > 0) {
            setPage((int) (((double) (page - 1) * tmp) / this.itemsPerPage) + 1);
        }

        return this.itemsPerPage;
    }

    /**
     * 获取开始索引，从0开始
     *
     * @return
     */
    public int getBeginIndex() {
        if (page >= 0) {
            return itemsPerPage * page;
        } else {
            return 0;
        }
    }

    /**
     * 获取结束索引
     *
     * @return
     */
    public int getEndIndex() {
        if (page >= 0) {
            return Math.min(itemsPerPage * (page + 1) - 1, items);
        } else {
            return 0;
        }
    }

    /**
     * 获取第一页
     *
     * @return
     */
    public int getFirstPage() {
        return calcPage(0);
    }

    /**
     * 获取最后一页
     *
     * @return
     */
    public int getLastPage() {
        return calcPage(getPages() - 1);
    }

    /**
     * 获取前一页
     *
     * @return
     */
    public int getPreviousPage() {
        return calcPage(page - 1);
    }

    /**
     * 获取前<code>n</code>页
     *
     * @param n
     * @return
     */
    public int getPreviousPage(int n) {
        return calcPage(page - n);
    }

    /**
     * 获取后一页
     *
     * @return
     */
    public int getNextPage() {
        return calcPage(page + 1);
    }

    /**
     * 获取后<code>n</code>页
     *
     * @param n
     * @return
     */
    public int getNextPage(int n) {
        return calcPage(page + n);
    }

    /**
     * 确保给定的页码不小于0，不超过总页码
     *
     * @param page
     * @return
     */
    protected int calcPage(int page) {
        int pages = getPages();

        if (pages > 0) {
            return (page < 0) ? 0 : ((page >= pages) ? (pages - 1) : page);
        }

        return 0;
    }

    /**
     * 取得默认大小(<code>DEFAULT_SLIDER_SIZE</code>)的页码滑动窗口，并将当前页尽可能地放在滑动窗口的中间部位。参见
     * {@link #getSlider(int n)}。
     *
     * @return 包含页码的数组
     */
    public int[] getSlider() {
        return getSlider(DEFAULT_SLIDER_SIZE);
    }

    /**
     * 取得指定大小的页码滑动窗口，并将当前页尽可能地放在滑动窗口的中间部位。例如: 总共有13页，当前页是第5页，取得一个大小为5的滑动窗口，将包括
     * 3，4，5，6, 7这几个页码，第5页被放在中间。如果当前页是12，则返回页码为 9，10，11，12，13。
     *
     * @param width 滑动窗口大小
     * @return 包含页码的数组，如果指定滑动窗口大小小于1或总页数为0，则返回空数组。
     */
    public int[] getSlider(int width) {
        int pages = getPages();

        if ((pages < 1) || (width < 1)) {
            return new int[0];
        } else {
            if (width > pages) {
                width = pages;
            }

            int[] slider = new int[width];
            int first = page - ((width - 1) / 2);

            if (first < 1) {
                first = 1;
            }

            if (((first + width) - 1) > pages) {
                first = pages - width + 1;
            }

            //显示的页码加1
            for (int i = 0; i < width; i++) {
                slider[i] = first + i;
            }

            return slider;
        }
    }

    /**
     * 转换成字符串表示。
     *
     * @return 字符串表示。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Paginator: page ");

        if (getPages() < 1) {
            sb.append(getPage());
        } else {
            int[] slider = getSlider();

            for (int i = 0; i < slider.length; i++) {
                sb.append(slider[i]);

                if (i < (slider.length - 1)) {
                    sb.append('\t');
                }
            }
        }

        sb.append(" of ").append(getPages()).append(",\n");
        sb.append("    Showing items ").append(getBeginIndex()).append(" to ")
                .append(getEndIndex()).append(" (total ").append(getItems())
                .append(" items), ");

        return sb.toString();
    }
}
