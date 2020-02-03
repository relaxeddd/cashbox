package ru.cashbox.android.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;
import retrofit2.Response;
import ru.cashbox.android.CashboxApplication;
import ru.cashbox.android.R;
import ru.cashbox.android.adapter.ElementAdapter;
import ru.cashbox.android.adapter.TechMapAdapter;
import ru.cashbox.android.adapter.TechMapAdapterGrid;
import ru.cashbox.android.auth.LoginTerminalActivity;
import ru.cashbox.android.model.Category;
import ru.cashbox.android.model.CategoryType;
import ru.cashbox.android.model.CheckItem;
import ru.cashbox.android.model.CheckItemCategoryType;
import ru.cashbox.android.model.Element;
import ru.cashbox.android.model.ElementType;
import ru.cashbox.android.model.ElementWrapper;
import ru.cashbox.android.model.Ingredient;
import ru.cashbox.android.model.ItemType;
import ru.cashbox.android.model.TechMap;
import ru.cashbox.android.model.TechMapElement;
import ru.cashbox.android.model.TechMapElementWrapper;
import ru.cashbox.android.model.TechMapGroup;
import ru.cashbox.android.model.TechMapModificator;
import ru.cashbox.android.query.CategoryQuery;
import ru.cashbox.android.query.TechMapQuery;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.saver.TechMapSlideSaver;
import ru.cashbox.android.utils.BillHelper;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class ElementFragment extends Fragment {

    private static final String CATEGORY_TAG = "CATEGORY";

    private GridView gridView;
    private Storage storage;
    private static boolean running = false;
    private ElementAdapter elementAdapter;
    private ElementWrapper elementWrapperMain;
    private CheckStateSaver checkStateSaver;
    private BreadcrumbsView breadcrumbsView;
    private TechMapSlideSaver slideSaver;
    private BillHelper billHelper;

    private Context appContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        appContext = CashboxApplication.getInstance().getContextProvider().getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_element, container, false);
        storage = Storage.getStorage();
        checkStateSaver = CheckStateSaver.getInstance();
        billHelper = BillHelper.getInstance();
        gridView = view.findViewById(R.id.element_grid_view);
        breadcrumbsView = view.findViewById(R.id.breadcrumbs_view);

        slideSaver = TechMapSlideSaver.getInstance();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Element element = (Element) adapterView.getItemAtPosition(position);
                switch (element.getElementType()) {
                    case GOOD:
                        Ingredient ingredient = (Ingredient) element;
                        CheckItem checkItem = new CheckItem(ingredient.getId(), ingredient.getImageUrl(),
                                ingredient.getName(), 1, ingredient.getPrice(),
                                1 * ingredient.getPrice(), CheckItemCategoryType.ITEM, new ArrayList<>());

                        if (checkStateSaver.getCurrentCheck() != null) {
                            checkStateSaver.addItemToCurrentCheck(checkItem);
                            billHelper.updateCurrentCheck();
                        } else {
                            billHelper.createCheckAndInsertItem(checkItem);
                        }
                        break;
                    case CATEGORY:
                        Category category = (Category) element;
                        List<Element> categoryElements = getElementsByCategoryId(category.getId(),
                                elementWrapperMain.getElements());
                        elementAdapter.setElements(categoryElements);
                        elementAdapter.notifyDataSetChanged();
                        breadcrumbsView.addItem(BreadcrumbItem.createSimpleItem(category.getName()));
                        break;
                    case TECHMAP:
                        TechMap techMap = (TechMap) element;
                        if (techMap.getModificatorGroups() == null || techMap.getModificatorGroups().isEmpty()) {
                            CheckItem checkItemTechMap = new CheckItem(techMap.getId(), techMap.getImageUrl(),
                                    techMap.getName(), 1, techMap.getPrice(), 1 * techMap.getPrice(),
                                    CheckItemCategoryType.TECHMAP, new ArrayList<>());

                            if (checkStateSaver.getCurrentCheck() != null) {
                                checkStateSaver.addItemToCurrentCheck(checkItemTechMap);
                                billHelper.updateCurrentCheck();
                            } else {
                                billHelper.createCheckAndInsertItem(checkItemTechMap);
                            }
                            return;
                        }
                        if (techMap.getModificatorGroups() != null) {
                            slideSaver.show();
                            slideSaver.setTotal(String.valueOf(techMap.getPrice()));

                            List<TechMapElementWrapper> data = new ArrayList<>();
                            TechMapAdapter techMapAdapter = new TechMapAdapter(view.getContext(),
                                    data, techMap.getName(), techMap.getId());
                            List<TechMapAdapterGrid.Section> sections = new ArrayList<>();

                            List<TechMapGroup> modificatorGroups = techMap.getModificatorGroups();
                            int lastPos = 0;
                            for (int i = 0; i < modificatorGroups.size(); i++) {
                                TechMapGroup techMapGroup = modificatorGroups.get(i);
                                List<TechMapModificator> modificators = techMapGroup.getModificators();
                                List<TechMapElement> techMapElements = new ArrayList<>();

                                for (int j = 0; j < modificators.size(); j++) {
                                    TechMapModificator techMapModificator = modificators.get(j);

                                    techMapElements.add(new TechMapElement(techMapModificator.getId(), j == 0,
                                            techMapModificator.getName(), techMapModificator.getImageUrl(),
                                            techMap.getPrice(), 1));
                                }

                                TechMapElementWrapper item = new TechMapElementWrapper(techMapGroup.getName(),
                                        techMapGroup.getModificatorsCount() == null
                                                ? 1 : techMapGroup.getModificatorsCount(), techMapElements);

                                if (techMapGroup.getModificatorsCount() == null) {
                                    techMapGroup.setModificatorsCount(1);
                                }

                                sections.add(new TechMapAdapterGrid.Section(lastPos,
                                        techMapGroup.getName() + " ("
                                                + techMapGroup.getModificatorsCount() + ")"));
                                lastPos += modificators.size();

                                data.add(item);
                            }

                            TechMapAdapterGrid.Section[] dummy = new TechMapAdapterGrid.Section[sections.size()];
                            TechMapAdapterGrid mSectionedAdapter = new TechMapAdapterGrid(getActivity(),
                                    R.layout.techmap_section, R.id.section_text, slideSaver.getTechMapGridView(), techMapAdapter);
                            mSectionedAdapter.setSections(sections.toArray(dummy));

                            slideSaver.getTechMapGridView().setAdapter(mSectionedAdapter);
                            slideSaver.setAdapter(techMapAdapter);
                        }
                }
            }
        });

        breadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {
            @Override
            public void onNavigateBack(BreadcrumbItem item, int position) {
                List<Element> elementsByCategoryName = getElementsByCategoryName(item.getSelectedItem());
                elementAdapter.setElements(elementsByCategoryName);
                elementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {

            }
        });

        new ElementLoaderTask(appContext).execute();

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class ElementLoaderTask extends AsyncTask<Void, Void, ElementWrapper> {
        private Context context;
        public ElementLoaderTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
        }

        @Override
        protected ElementWrapper doInBackground(Void... voids) {
            int code = 0;
            List<Element> elements = new ArrayList<>();

            try {
                CategoryQuery categoryQuery = RetrofitInstance.getRetrofit()
                        .create(CategoryQuery.class);
                TechMapQuery techMapQuery = RetrofitInstance.getRetrofit()
                        .create(TechMapQuery.class);

                Response<List<Category>> categoryResponse = categoryQuery
                        .getAllCategories(storage.getUserEmployeeSession().getToken(),
                                true).execute();
                Response<List<TechMap>> techMapResponse = techMapQuery
                        .getAllTechMaps(storage.getUserEmployeeSession().getToken()).execute();

                code = categoryResponse.code();
                if (categoryResponse.body() != null && techMapResponse.body() != null) {
                    elements.addAll(categoryResponse.body());
                    elements.addAll(techMapResponse.body());
                }
            } catch (IOException e) {
                Log.e(CATEGORY_TAG, context.getString(R.string.internal_error), e);
            }

            return new ElementWrapper(code, elements);
        }

        @Override
        protected void onPostExecute(ElementWrapper result) {
            super.onPostExecute(result);
            running = false;
            if (result == null) {
                Toast.makeText(storage.getContext(), context.getString(R.string.server_connect_error),
                        Toast.LENGTH_SHORT).show();
                Log.e(CATEGORY_TAG, context.getString(R.string.server_connect_error));
                return;
            }

            switch (result.getCode()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    getActivity().finishAffinity();
                    startActivity(new Intent(storage.getContext(), LoginTerminalActivity.class));
                    Log.i(CATEGORY_TAG, context.getString(R.string.token_outdated));
                    break;
                case HttpStatus.SC_OK:
                    if (result.getElements() != null) {
                        elementWrapperMain = result;
                        Long mainCategoryId = getMainCategoryId(result.getElements());
                        Element categoryById = getCategoryById(mainCategoryId);
                        if (categoryById != null) {
                            List<Element> elementsByCategoryId = getElementsByCategoryId(mainCategoryId, result.getElements());
                            elementAdapter = new ElementAdapter(storage.getContext(), elementsByCategoryId);
                            gridView.setAdapter(elementAdapter);
                            breadcrumbsView.addItem(BreadcrumbItem.createSimpleItem(categoryById.getName()));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Long getMainCategoryId(List<Element> elements) {
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element instanceof Category) {
                Category category = (Category) element;
                if (category.getType().equals(CategoryType.TERMINAL)
                        && category.getDefaultCategory()) {
                    return category.getId();
                }
            }
        }
        return null;
    }

    private Element getCategoryById(Long id) {
        for (int i = 0; i < elementWrapperMain.getElements().size(); i++) {
            Element element = elementWrapperMain.getElements().get(i);
            if (element instanceof Element) {
                Category category = (Category) elementWrapperMain.getElements().get(i);
                if (category.getId() != null && category.getId().equals(id)) {
                    return category;
                }
            }
        }
        return null;
    }

    private List<Element> getElementsByCategoryName(String name) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < elementWrapperMain.getElements().size(); i++) {
            Element element = elementWrapperMain.getElements().get(i);
            if (element instanceof Element) {
                Category category = (Category) element;
                if (category.getName().endsWith(name)) {
                    elements = getElementsByCategoryId(category.getId(),
                            elementWrapperMain.getElements());
                    break;
                }
            }
        }
        return elements;
    }

    private List<Element> getElementsByCategoryId(Long id, List<Element> allElements) {
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < allElements.size(); i++) {
            Element element = allElements.get(i);
            if (element instanceof Category) {
                Category category = (Category) element;
                if (category.getId().equals(id)) {
                    if (category.getIngredients() != null) {
                        for (int j = 0; j < category.getIngredients().size(); j++) {
                            Ingredient ingredient = category.getIngredients().get(j);
                            if (ingredient.getType().equals(ItemType.GOOD)) {
                                elements.add(ingredient);
                            }
                        }
                    }
                }
                if (category.getParentId() != null && category.getParentId().equals(id)
                        && category.getType().equals(CategoryType.TERMINAL)) {
                    elements.add(category);
                }
            }
            if (element instanceof TechMap) {
                TechMap techMap = (TechMap) element;
                if (techMap.getCategory().getId().equals(id)) {
                    elements.add(techMap);
                }
            }
        }

        return elements;
    }

}