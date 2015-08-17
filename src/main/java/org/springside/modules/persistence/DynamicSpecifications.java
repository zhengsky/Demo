package org.springside.modules.persistence;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springside.modules.utils.Collections3;

public class DynamicSpecifications {
	public static <T> Specification<T> bySearchFilter(Collection<SearchFilter> filters, Class<T> entityClazz)
  {
    new Specification()
    {
      public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder)
      {
        if (Collections3.isNotEmpty(this.val$filters))
        {
          List<Predicate> predicates = Lists.newArrayList();
          for (SearchFilter filter : this.val$filters)
          {
            String[] names = StringUtils.split(filter.fieldName, ".");
            Path expression = root.get(names[0]);
            for (int i = 1; i < names.length; i++) {
              expression = expression.get(names[i]);
            }
            switch (DynamicSpecifications.2.$SwitchMap$org$springside$modules$persistence$SearchFilter$Operator[filter.operator.ordinal()])
            {
            case 1: 
              predicates.add(builder.equal(expression, filter.value));
              break;
            case 2: 
              predicates.add(builder.like(expression, "%" + filter.value + "%"));
              break;
            case 3: 
              predicates.add(builder.greaterThan(expression, (Comparable)filter.value));
              break;
            case 4: 
              predicates.add(builder.lessThan(expression, (Comparable)filter.value));
              break;
            case 5: 
              predicates.add(builder.greaterThanOrEqualTo(expression, (Comparable)filter.value));
              break;
            case 6: 
              predicates.add(builder.lessThanOrEqualTo(expression, (Comparable)filter.value));
            }
          }
          if (!predicates.isEmpty()) {
            return builder.and((Predicate[])predicates.toArray(new Predicate[predicates.size()]));
          }
        }
        return builder.conjunction();
      }
    };
  }
}
